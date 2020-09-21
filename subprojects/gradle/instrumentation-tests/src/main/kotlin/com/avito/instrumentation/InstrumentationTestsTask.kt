package com.avito.instrumentation

import com.avito.android.stats.statsdConfig
import com.avito.bitbucket.bitbucketConfig
import com.avito.bitbucket.pullRequestId
import com.avito.cd.buildOutput
import com.avito.gradle.worker.inMemoryWork
import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.Data
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.filter.ImpactAnalysisResult
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class InstrumentationTestsTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @Optional
    @InputFile
    val application: RegularFileProperty = objects.fileProperty()

    @InputFile
    val testApplication: RegularFileProperty = objects.fileProperty()

    @Input
    val impactAnalysisPolicy = objects.property<ImpactAnalysisPolicy>()

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
    val apkOnTargetCommit: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    val testApkOnTargetCommit: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    val applicationProguardMapping: RegularFileProperty = objects.fileProperty()

    @Optional
    @InputFile
    val testProguardMapping: RegularFileProperty = objects.fileProperty()

    @Input
    val sendStatistics = objects.property<Boolean>()

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
    val suppressFailure = objects.property<Boolean>().convention(false)

    @Input
    val suppressFlaky = objects.property<Boolean>().convention(false)

    @Input
    val instrumentationConfiguration = objects.property<InstrumentationConfiguration.Data>()

    @Input
    val parameters = objects.property<ExecutionParameters>()

    @Internal
    val reportViewerConfig = objects.property(Data.ReportViewer::class.java)

    @Internal
    val registry = objects.property<String>()

    @Internal
    val unitToChannelMapping = objects.mapProperty<Team, SlackChannel>()

    @Internal
    val kubernetesCredentials = objects.property<KubernetesCredentials>()

    @OutputDirectory
    val output: DirectoryProperty = objects.directoryProperty()

    @TaskAction
    fun doWork() {
        val configuration = instrumentationConfiguration.get()
        val reportCoordinates = configuration.instrumentationParams.reportCoordinates()
        val reportConfig = createReportConfig(reportCoordinates)
        val reportFactory = createReportFactory()

        saveTestResultsToBuildOutput(
            reportFactory,
            reportConfig
        )

        //todo новое api, когда выйдет в stable
        // https://docs.gradle.org/5.6/userguide/custom_tasks.html#using-the-worker-api
        workerExecutor.inMemoryWork {
            InstrumentationTestsAction(
                InstrumentationTestsAction.Params(
                    mainApk = application.orNull?.asFile,
                    testApk = testApplication.get().asFile,
                    apkOnTargetCommit = apkOnTargetCommit.orNull?.asFile,
                    testApkOnTargetCommit = testApkOnTargetCommit.orNull?.asFile,
                    instrumentationConfiguration = configuration,
                    executionParameters = parameters.get(),
                    buildId = buildId.get(),
                    buildType = buildType.get(),
                    buildUrl = buildUrl.get(),
                    kubernetesCredentials = requireNotNull(kubernetesCredentials.orNull) { "you need to provide kubernetesCredentials" },
                    projectName = project.name,
                    currentBranch = gitBranch.get(),
                    sourceCommitHash = sourceCommitHash.get(),
                    suppressFailure = suppressFailure.getOrElse(false),
                    suppressFlaky = suppressFlaky.getOrElse(false),
                    impactAnalysisResult = ImpactAnalysisResult.create(
                        policy = impactAnalysisPolicy.get(),
                        affectedTestsFile = affectedTests.asFile.orNull,
                        addedTestsFile = newTests.asFile.orNull,
                        modifiedTestsFile = modifiedTests.asFile.orNull
                    ),
                    logger = ciLogger,
                    outputDir = output.get().asFile,
                    sendStatistics = sendStatistics.get(),
                    slackToken = slackToken.get(),
                    fileStorageUrl = getFileStorageUrl(),
                    pullRequestId = project.pullRequestId.orNull,
                    bitbucketConfig = project.bitbucketConfig.get(),
                    statsdConfig = project.statsdConfig.get(),
                    unitToChannelMapping = unitToChannelMapping.get(),
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
            // todo Extract Instrumentation contract to module. Upload cd task will depend on it and consume Instrumentation result
            val buildOutput = project.buildOutput.get()
            val testResults = getTestResultsAction.getTestResults()
            buildOutput.testResults[configuration.name] = testResults
        }
    }

    private fun createReportFactory(): Report.Factory {
        val reportViewerConfig = reportViewerConfig.orNull
        val factories = mutableMapOf<Class<out Report.Factory.Config>, Report.Factory>()
        if (reportViewerConfig != null) {
            factories.put(
                Report.Factory.Config.ReportViewerCoordinates::class.java, Report.Factory.ReportViewerFactory(
                    reportApiUrl = reportViewerConfig.reportApiUrl,
                    reportApiFallbackUrl = reportViewerConfig.reportApiFallbackUrl,
                    ciLogger = ciLogger
                )
            )
        }
        factories.put(
            Report.Factory.Config.InMemory::class.java, Report.Factory.InMemoryReportFactory()
        )
        return Report.Factory.StrategyFactory(
            factories = factories.toMap()
        )
    }
}
