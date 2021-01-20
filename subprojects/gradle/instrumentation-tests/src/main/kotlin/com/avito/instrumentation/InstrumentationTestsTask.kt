package com.avito.instrumentation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.build_verdict.span.SpannedString.Companion.link
import com.avito.android.getApk
import com.avito.android.getApkOrThrow
import com.avito.cd.buildOutput
import com.avito.gradle.worker.inMemoryWork
import com.avito.instrumentation.InstrumentationTestsActionFactory.Companion.gson
import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.Data
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.filter.ImpactAnalysisResult
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.LoggerFactory
import com.avito.report.model.ReportCoordinates
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
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
abstract class InstrumentationTestsTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask(), BuildVerdictTask {

    internal class Verdict(
        val reportUrl: String,
        val testRunVerdict: TestRunResult.Verdict
    )

    @Optional
    @InputDirectory
    val application: DirectoryProperty = objects.directoryProperty()

    @InputDirectory
    val testApplication: DirectoryProperty = objects.directoryProperty()

    @Input
    val impactAnalysisPolicy: Property<ImpactAnalysisPolicy> =
        objects.property<ImpactAnalysisPolicy>().convention(ImpactAnalysisPolicy.Off)

    @Optional
    @InputFile
    val newTests: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    val modifiedTests: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    val affectedTests: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    val changedTests: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    val applicationProguardMapping: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    val testProguardMapping: RegularFileProperty = objects.fileProperty()

    @Input
    val slackToken = objects.property<String>()

    @Input
    val buildId = objects.property<String>()

    @Input
    val buildType = objects.property<String>()

    @Input
    val buildUrl = objects.property<String>()

    @Input
    val defaultBranch = objects.property<String>()

    @Input
    val gitCommit = objects.property<String>()

    @Input
    val gitBranch = objects.property<String>()

    @Input
    val sourceCommitHash = objects.property<String>()

    @Input
    val suppressFailure: Property<Boolean> = objects.property<Boolean>().convention(false)

    @Input
    val suppressFlaky: Property<Boolean> = objects.property<Boolean>().convention(false)

    @Input
    val instrumentationConfiguration = objects.property<InstrumentationConfiguration.Data>()

    @Input
    val parameters = objects.property<ExecutionParameters>()

    @Internal
    val reportViewerConfig: Property<Data.ReportViewer> = objects.property(Data.ReportViewer::class.java)

    @Internal
    val registry = objects.property<String>()

    @Internal
    val kubernetesCredentials = objects.property<KubernetesCredentials>()

    @OutputDirectory
    val output: DirectoryProperty = objects.directoryProperty()

    private val verdictFile = objects.fileProperty().convention(output.file("verdict.json"))

    @get:Internal
    override val verdict: SpannedString
        get() {
            val verdictRaw = verdictFile.asFile.get().reader()
            val verdict = gson.fromJson<Verdict>(verdictRaw)
            return link(
                url = verdict.reportUrl,
                text = verdict.testRunVerdict.message
            )
        }

    @TaskAction
    fun doWork() {
        val configuration = instrumentationConfiguration.get()
        val reportCoordinates = configuration.instrumentationParams.reportCoordinates()
        val reportConfig = createReportConfig(reportCoordinates)
        val loggerFactory = GradleLoggerFactory.fromTask(this)
        val timeProvider: TimeProvider = DefaultTimeProvider()

        val reportFactory = createReportFactory(
            loggerFactory = loggerFactory,
            timeProvider = timeProvider
        )

        saveTestResultsToBuildOutput(
            reportFactory,
            reportConfig
        )

        workerExecutor.inMemoryWork {
            InstrumentationTestsAction(
                InstrumentationTestsAction.Params(
                    mainApk = application.orNull?.getApk(),
                    testApk = testApplication.get().getApkOrThrow(),
                    instrumentationConfiguration = configuration,
                    executionParameters = parameters.get(),
                    buildId = buildId.get(),
                    buildType = buildType.get(),
                    buildUrl = buildUrl.get(),
                    kubernetesCredentials = requireNotNull(kubernetesCredentials.orNull) {
                        "you need to provide kubernetesCredentials"
                    },
                    projectName = project.name,
                    currentBranch = gitBranch.get(),
                    sourceCommitHash = sourceCommitHash.get(),
                    suppressFailure = suppressFailure.getOrElse(false),
                    suppressFlaky = suppressFlaky.getOrElse(false),
                    impactAnalysisResult = ImpactAnalysisResult.create(
                        policy = impactAnalysisPolicy.get(),
                        affectedTestsFile = affectedTests.asFile.orNull,
                        addedTestsFile = newTests.asFile.orNull,
                        modifiedTestsFile = modifiedTests.asFile.orNull,
                        changedTestsFile = changedTests.asFile.orNull
                    ),
                    loggerFactory = loggerFactory,
                    outputDir = output.get().asFile,
                    verdictFile = verdictFile.get().asFile,
                    slackToken = slackToken.get(),
                    fileStorageUrl = getFileStorageUrl(),
                    reportViewerUrl = reportViewerConfig.orNull?.reportViewerUrl
                        ?: "http://stub", // stub for inmemory report
                    registry = registry.get(),
                    reportConfig = reportConfig,
                    reportFactory = reportFactory,
                    reportCoordinates = reportCoordinates,
                    proguardMappings = listOf(
                        applicationProguardMapping,
                        testProguardMapping
                    ).mapNotNull { it.orNull?.asFile }
                )
            ).run()
        }
    }

    /**
     * todo FileStorage needed only for ReportViewer
     */
    private fun getFileStorageUrl(): String {
        return reportViewerConfig.orNull?.fileStorageUrl ?: ""
    }

    private fun createReportConfig(
        reportCoordinates: ReportCoordinates
    ): Report.Factory.Config {
        return if (reportViewerConfig.isPresent) {
            Report.Factory.Config.ReportViewerCoordinates(
                reportCoordinates = reportCoordinates,
                buildId = buildId.get()
            )
        } else {
            Report.Factory.Config.InMemory(buildId.get())
        }
    }

    /**
     * todo Move into Report.Impl
     */
    private fun saveTestResultsToBuildOutput(
        reportFactory: Report.Factory,
        reportConfig: Report.Factory.Config
    ) {
        val configuration = instrumentationConfiguration.get()
        val reportCoordinates = configuration.instrumentationParams.reportCoordinates()
        val reportViewerConfig = reportViewerConfig.orNull
        if (reportViewerConfig != null && reportConfig is Report.Factory.Config.ReportViewerCoordinates) {
            val getTestResultsAction = GetTestResultsAction(
                reportViewerUrl = reportViewerConfig.reportViewerUrl,
                reportCoordinates = reportCoordinates,
                report = reportFactory.createReport(reportConfig),
                gitBranch = gitBranch.get(),
                gitCommit = gitCommit.get()
            )
            // todo move that logic to task output. Instrumentation task mustn't know about Upload CD models
            // todo Extract Instrumentation contract to module.
            //  Upload cd task will depend on it and consume Instrumentation result
            val buildOutput = project.buildOutput.get()
            val testResults = getTestResultsAction.getTestResults()
            buildOutput.testResults[configuration.name] = testResults
        }
    }

    private fun createReportFactory(
        loggerFactory: LoggerFactory,
        timeProvider: TimeProvider
    ): Report.Factory {
        val reportViewerConfig = reportViewerConfig.orNull
        val factories = mutableMapOf<String, Report.Factory>()
        if (reportViewerConfig != null) {
            factories[Report.Factory.Config.ReportViewerCoordinates::class.java.simpleName] =
                Report.Factory.ReportViewerFactory(
                    reportApiUrl = reportViewerConfig.reportApiUrl,
                    reportApiFallbackUrl = reportViewerConfig.reportApiFallbackUrl,
                    loggerFactory = loggerFactory,
                    timeProvider = timeProvider,
                    verboseHttp = false // do not enable for production, generates a ton of logs
                )
        }

        factories[Report.Factory.Config.InMemory::class.java.simpleName] =
            Report.Factory.InMemoryReportFactory(timeProvider = timeProvider)

        return Report.Factory.StrategyFactory(
            factories = factories.toMap()
        )
    }
}
