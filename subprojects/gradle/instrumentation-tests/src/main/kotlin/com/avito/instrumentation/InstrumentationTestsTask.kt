package com.avito.instrumentation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.build_verdict.span.SpannedString.Companion.link
import com.avito.android.build_verdict.span.SpannedString.Companion.multiline
import com.avito.android.getApk
import com.avito.android.getApkOrThrow
import com.avito.android.runner.report.ReportViewerConfig
import com.avito.android.stats.statsdConfig
import com.avito.cd.buildOutput
import com.avito.gradle.worker.inMemoryWork
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.Data
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory.Companion.gson
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.finalizer.GetTestResultsAction
import com.avito.instrumentation.internal.finalizer.verdict.InstrumentationTestsTaskVerdict
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.instrumentation.service.TestRunParams
import com.avito.instrumentation.service.TestRunnerService
import com.avito.instrumentation.service.TestRunnerWorkAction
import com.avito.logger.GradleLoggerFactory
import com.avito.utils.gradle.KubernetesCredentials
import com.github.salomonbrys.kotson.fromJson
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public abstract class InstrumentationTestsTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask(), BuildVerdictTask {

    @Optional
    @InputDirectory
    public val application: DirectoryProperty = objects.directoryProperty()

    @InputDirectory
    public val testApplication: DirectoryProperty = objects.directoryProperty()

    @Input
    public val runOnlyChangedTests: Property<Boolean> = objects.property()

    @Optional
    @InputFile
    public val changedTests: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    public val applicationProguardMapping: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    public val testProguardMapping: RegularFileProperty = objects.fileProperty()

    @Input
    public val buildId: Property<String> = objects.property()

    @Input
    public val buildType: Property<String> = objects.property()

    @Input
    public val gitCommit: Property<String> = objects.property()

    @Input
    public val gitBranch: Property<String> = objects.property()

    @Input
    public val useInMemoryReport: Property<Boolean> = objects.property()

    @Input
    public val suppressFailure: Property<Boolean> = objects.property<Boolean>().convention(false)

    @Input
    public val suppressFlaky: Property<Boolean> = objects.property<Boolean>().convention(false)

    @Input
    public val instrumentationConfiguration: Property<InstrumentationConfiguration.Data> = objects.property()

    @Input
    public val parameters: Property<ExecutionParameters> = objects.property()

    @Input
    public val uploadAllTestArtifacts: Property<Boolean> = objects.property<Boolean>().convention(false)

    @Internal
    public val reportViewerProperty: Property<Data.ReportViewer> = objects.property(Data.ReportViewer::class.java)

    @Internal
    public val kubernetesCredentials: Property<KubernetesCredentials> = objects.property()

    @Internal
    public val testRunnerService: Property<TestRunnerService> = objects.property()

    @OutputDirectory
    public val output: DirectoryProperty = objects.directoryProperty()

    private val verdictFile = objects.fileProperty().convention(output.file("verdict.json"))

    @get:Internal
    override val verdict: SpannedString
        get() {
            val verdictRaw = verdictFile.asFile.get().reader()
            val verdict = gson.fromJson<InstrumentationTestsTaskVerdict>(verdictRaw)
            return multiline(
                mutableListOf<SpannedString>()
                    .apply {
                        add(link(verdict.reportUrl, verdict.title))
                        addAll(verdict.causeFailureTests.map { test ->
                            link(test.testUrl, test.title)
                        })
                    }
            )
        }

    @TaskAction
    public fun doWork() {
        val configuration = instrumentationConfiguration.get()
        val reportCoordinates = configuration.instrumentationParams.reportCoordinates()
        val loggerFactory = GradleLoggerFactory.fromTask(this)

        saveTestResultsToBuildOutput()

        val statsDConfig = project.statsdConfig.get()

        val reportViewerData = reportViewerProperty.orNull
        val reportViewerConfig = if (reportViewerData != null) {
            ReportViewerConfig(reportViewerData.reportApiUrl, reportCoordinates)
        } else {
            null
        }

        val testRunParams = InstrumentationTestsAction.Params(
            mainApk = application.orNull?.getApk(),
            testApk = testApplication.get().getApkOrThrow(),
            instrumentationConfiguration = configuration,
            executionParameters = parameters.get(),
            buildId = buildId.get(),
            buildType = buildType.get(),
            kubernetesCredentials = requireNotNull(kubernetesCredentials.orNull) {
                "you need to provide kubernetesCredentials"
            },
            projectName = project.name,
            suppressFailure = suppressFailure.getOrElse(false),
            suppressFlaky = suppressFlaky.getOrElse(false),
            impactAnalysisResult = ImpactAnalysisResult.create(
                runOnlyChangedTests = runOnlyChangedTests.get(),
                changedTestsFile = changedTests.asFile.orNull
            ),
            loggerFactory = loggerFactory,
            outputDir = output.get().asFile,
            verdictFile = verdictFile.get().asFile,
            fileStorageUrl = getFileStorageUrl(),
            statsDConfig = statsDConfig,
            proguardMappings = listOf(
                applicationProguardMapping,
                testProguardMapping
            ).mapNotNull { it.orNull?.asFile },
            useInMemoryReport = useInMemoryReport.get(),
            uploadTestArtifacts = uploadAllTestArtifacts.get(),
            reportViewerConfig = reportViewerConfig
        )

        if (testRunnerService.isPresent) {
            val service = testRunnerService.get()
            val queue = workerExecutor.noIsolation()

            queue.submit(TestRunnerWorkAction::class.java) { params ->
                params.service.set(service)
                params.statsDConfig.set(statsDConfig)
                params.testRunParams.set(
                    TestRunParams(
                        projectName = project.name,
                        instrumentationConfigName = configuration.name
                    )
                )
                params.legacyTestRunParams.set(testRunParams)
            }
        } else {
            workerExecutor.inMemoryWork {
                InstrumentationTestsAction(testRunParams).run()
            }
        }
    }

    /**
     * todo FileStorage needed only for ReportViewer
     */
    private fun getFileStorageUrl(): String {
        return reportViewerProperty.orNull?.fileStorageUrl ?: "http://stub"
    }

    /**
     * todo Move into Report.Impl
     */
    private fun saveTestResultsToBuildOutput() {
        val configuration = instrumentationConfiguration.get()
        val reportCoordinates = configuration.instrumentationParams.reportCoordinates()
        val reportViewerConfig = reportViewerProperty.orNull
        if (reportViewerConfig != null) {
            val getTestResultsAction = GetTestResultsAction(
                reportViewerUrl = reportViewerConfig.reportViewerUrl,
                reportCoordinates = reportCoordinates
            )
            // todo move that logic to task output. Instrumentation task mustn't know about Upload CD models
            // todo Extract Instrumentation contract to module.
            //  Upload cd task will depend on it and consume Instrumentation result
            val buildOutput = project.buildOutput.get()
            val testResults = getTestResultsAction.getTestResults()
            buildOutput.testResults[configuration.name] = testResults
        }
    }
}
