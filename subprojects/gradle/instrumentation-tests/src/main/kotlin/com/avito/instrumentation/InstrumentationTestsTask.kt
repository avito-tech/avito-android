package com.avito.instrumentation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.build_verdict.span.SpannedString.Companion.link
import com.avito.android.build_verdict.span.SpannedString.Companion.multiline
import com.avito.android.getApk
import com.avito.android.getApkOrThrow
import com.avito.android.runner.report.StrategyFactory
import com.avito.android.runner.report.factory.InMemoryReportFactory
import com.avito.android.runner.report.factory.ReportFactory
import com.avito.android.runner.report.factory.ReportViewerFactory
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.statsdConfig
import com.avito.cd.buildOutput
import com.avito.gradle.worker.inMemoryWork
import com.avito.http.HttpClientProvider
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.Data
import com.avito.instrumentation.internal.GetTestResultsAction
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory.Companion.gson
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.instrumentation.internal.verdict.InstrumentationTestsTaskVerdict
import com.avito.instrumentation.service.TestRunParams
import com.avito.instrumentation.service.TestRunnerService
import com.avito.instrumentation.service.TestRunnerWorkAction
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
    public val suppressFailure: Property<Boolean> = objects.property<Boolean>().convention(false)

    @Input
    public val suppressFlaky: Property<Boolean> = objects.property<Boolean>().convention(false)

    @Input
    public val instrumentationConfiguration: Property<InstrumentationConfiguration.Data> = objects.property()

    @Input
    public val parameters: Property<ExecutionParameters> = objects.property()

    @Internal
    public val reportViewerConfig: Property<Data.ReportViewer> = objects.property(Data.ReportViewer::class.java)

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
        val reportConfig = createReportConfig(reportCoordinates)
        val loggerFactory = GradleLoggerFactory.fromTask(this)
        val timeProvider: TimeProvider = DefaultTimeProvider()
        val httpClientProvider = HttpClientProvider(
            statsDSender = StatsDSender.Impl(
                config = project.statsdConfig.get(),
                loggerFactory = loggerFactory
            ),
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        )

        val reportFactory = createReportFactory(
            loggerFactory = loggerFactory,
            timeProvider = timeProvider,
            httpClientProvider = httpClientProvider
        )

        saveTestResultsToBuildOutput(
            reportFactory,
            reportConfig
        )

        val statsDConfig = project.statsdConfig.get()

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
            reportViewerUrl = reportViewerConfig.orNull?.reportViewerUrl
                ?: "http://stub",
            fileStorageUrl = getFileStorageUrl(),
            statsDConfig = statsDConfig,
            reportFactory = reportFactory,
            reportConfig = reportConfig,
            reportCoordinates = reportCoordinates, // stub for inmemory report
            proguardMappings = listOf(
                applicationProguardMapping,
                testProguardMapping
            ).mapNotNull { it.orNull?.asFile }
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
        return reportViewerConfig.orNull?.fileStorageUrl ?: "http://stub"
    }

    private fun createReportConfig(
        reportCoordinates: ReportCoordinates
    ): ReportFactory.Config {
        return if (reportViewerConfig.isPresent) {
            ReportFactory.Config.ReportViewerCoordinates(
                reportCoordinates = reportCoordinates,
                buildId = buildId.get()
            )
        } else {
            ReportFactory.Config.InMemory(buildId.get())
        }
    }

    /**
     * todo Move into Report.Impl
     */
    private fun saveTestResultsToBuildOutput(
        reportFactory: ReportFactory,
        reportConfig: ReportFactory.Config
    ) {
        val configuration = instrumentationConfiguration.get()
        val reportCoordinates = configuration.instrumentationParams.reportCoordinates()
        val reportViewerConfig = reportViewerConfig.orNull
        if (reportViewerConfig != null && reportConfig is ReportFactory.Config.ReportViewerCoordinates) {
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
        timeProvider: TimeProvider,
        httpClientProvider: HttpClientProvider
    ): ReportFactory {
        val reportViewerConfig = reportViewerConfig.orNull
        val factories = mutableMapOf<String, ReportFactory>()
        if (reportViewerConfig != null) {
            factories[ReportFactory.Config.ReportViewerCoordinates::class.java.simpleName] =
                ReportViewerFactory(
                    reportApiUrl = reportViewerConfig.reportApiUrl,
                    loggerFactory = loggerFactory,
                    timeProvider = timeProvider,
                    httpClientProvider = httpClientProvider
                )
        }

        factories[ReportFactory.Config.InMemory::class.java.simpleName] =
            InMemoryReportFactory(timeProvider = timeProvider)

        return StrategyFactory(
            factories = factories.toMap()
        )
    }
}
