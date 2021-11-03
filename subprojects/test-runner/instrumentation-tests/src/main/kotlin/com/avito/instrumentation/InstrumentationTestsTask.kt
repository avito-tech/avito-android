package com.avito.instrumentation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.build_verdict.span.SpannedString.Companion.link
import com.avito.android.build_verdict.span.SpannedString.Companion.multiline
import com.avito.android.getApk
import com.avito.android.getApkOrThrow
import com.avito.android.stats.StatsDConfig
import com.avito.gradle.worker.inMemoryWork
import com.avito.instrumentation.configuration.Experiments
import com.avito.instrumentation.configuration.ReportViewer
import com.avito.instrumentation.internal.RunnerInputDumper
import com.avito.logger.GradleLoggerPlugin
import com.avito.runner.config.InstrumentationConfigurationData
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.finalizer.verdict.InstrumentationTestsTaskVerdict
import com.avito.runner.scheduler.report.ReportViewerConfig
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.runner.scheduler.TestSchedulerFactoryProvider
import com.avito.runner.scheduler.runner.scheduler.TestSchedulerResult
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisResult
import com.avito.utils.BuildFailer
import com.avito.utils.gradle.KubernetesCredentials
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

public abstract class InstrumentationTestsTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask(), BuildVerdictTask {

    @get:Optional
    @get:InputDirectory
    public abstract val application: DirectoryProperty

    @get:Optional
    @get:Input
    public abstract val applicationPackageName: Property<String>

    @get:InputDirectory
    public abstract val testApplication: DirectoryProperty

    @get:Input
    public abstract val testApplicationPackageName: Property<String>

    @get:Input
    public abstract val runOnlyChangedTests: Property<Boolean>

    @get:Optional
    @get:InputFile
    public abstract val changedTests: RegularFileProperty

    @get:Optional
    @get:InputFile
    public abstract val applicationProguardMapping: RegularFileProperty

    @get:Optional
    @get:InputFile
    public abstract val testProguardMapping: RegularFileProperty

    @get:Input
    public abstract val buildId: Property<String>

    @get:Input
    public abstract val buildType: Property<String>

    @get:Input
    public abstract val gitCommit: Property<String>

    @get:Input
    public abstract val gitBranch: Property<String>

    @get:Input
    public abstract val experiments: Property<Experiments>

    @get:Input
    public abstract val suppressFailure: Property<Boolean>

    @get:Input
    public abstract val suppressFlaky: Property<Boolean>

    @get:Input
    public abstract val instrumentationConfiguration: Property<InstrumentationConfigurationData>

    @get:Input
    public abstract val instrumentationRunner: Property<String>

    @get:Input
    public abstract val logcatTags: SetProperty<String>

    @get:Input
    public abstract val enableDeviceDebug: Property<Boolean>

    @get:Input
    @get:Optional
    public abstract val reportViewerProperty: Property<ReportViewer>

    @get:Input
    public abstract val gradleTestKitRun: Property<Boolean>

    @get:Input
    public abstract val projectName: Property<String>

    @get:Internal
    public abstract val kubernetesCredentials: Property<KubernetesCredentials>

    @get:Internal
    public abstract val buildFailer: Property<BuildFailer>

    @get:Internal
    public abstract val statsDConfig: Property<StatsDConfig>

    @get:OutputDirectory
    public abstract val output: DirectoryProperty

    private val verdictFile = objects.fileProperty().convention(output.file("verdict.json"))

    @get:Internal
    override val verdict: SpannedString
        get() {
            val gson: Gson = GsonBuilder().setPrettyPrinting().create()
            val verdictRaw = verdictFile.asFile.get().reader()
            val verdict = gson.fromJson(verdictRaw, InstrumentationTestsTaskVerdict::class.java)
            return multiline(
                mutableListOf<SpannedString>()
                    .apply {
                        add(link(verdict.reportUrl, verdict.title))
                        addAll(verdict.problemTests.map { test ->
                            link(test.testUrl, test.title)
                        })
                    }
            )
        }

    @TaskAction
    public fun doWork() {
        val configuration = instrumentationConfiguration.get()
        val reportCoordinates = configuration.instrumentationParams.reportCoordinates()

        val reportViewerData = reportViewerProperty.orNull
        val reportViewerConfig = if (reportViewerData != null) {
            ReportViewerConfig(
                apiUrl = reportViewerData.reportApiUrl,
                viewerUrl = reportViewerData.reportViewerUrl,
                reportCoordinates = reportCoordinates
            )
        } else {
            null
        }

        val experiments = experiments.get()

        val output = output.get().asFile

        val testRunParams = RunnerInputParams(
            mainApk = application.orNull?.getApk(),
            testApk = testApplication.get().getApkOrThrow(),
            instrumentationConfiguration = configuration,
            executionParameters = ExecutionParameters(
                applicationPackageName.get(),
                testApplicationPackageName.get(),
                instrumentationRunner.get(),
                logcatTags.get(),
            ),
            buildId = buildId.get(),
            buildType = buildType.get(),
            kubernetesCredentials = requireNotNull(kubernetesCredentials.orNull) {
                "you need to provide kubernetesCredentials"
            },
            deviceDebug = enableDeviceDebug.get(),
            projectName = projectName.get(),
            suppressFailure = suppressFailure.get(),
            suppressFlaky = suppressFlaky.get(),
            impactAnalysisResult = ImpactAnalysisResult.create(
                runOnlyChangedTests = runOnlyChangedTests.get(),
                changedTestsFile = changedTests.asFile.orNull
            ),
            outputDir = output,
            verdictFile = verdictFile.get().asFile,
            fileStorageUrl = getFileStorageUrl(),
            statsDConfig = statsDConfig.get(),
            proguardMappings = listOf(
                applicationProguardMapping,
                testProguardMapping
            ).mapNotNull { it.orNull?.asFile },
            uploadTestArtifacts = experiments.uploadArtifactsFromRunner,
            reportViewerConfig = reportViewerConfig,
            fetchLogcatForIncompleteTests = experiments.fetchLogcatForIncompleteTests,
            saveTestArtifactsToOutputs = experiments.saveTestArtifactsToOutputs,
            useLegacyExtensionsV1Beta = experiments.useLegacyExtensionsV1Beta,
            sendPodsMetrics = experiments.sendPodsMetrics,
        )

        val isGradleTestKitRun = gradleTestKitRun.get()

        RunnerInputDumper(output).dumpInput(
            input = testRunParams,
            isGradleTestKitRun = isGradleTestKitRun
        )

        if (!isGradleTestKitRun) {
            val loggerFactory = GradleLoggerPlugin.getLoggerFactory(this).get()
            workerExecutor.inMemoryWork {
                when (
                    val result = TestSchedulerFactoryProvider(loggerFactory)
                        .provide(testRunParams)
                        .create()
                        .schedule()
                ) {
                    TestSchedulerResult.Ok -> {
                        // do nothing
                    }
                    is TestSchedulerResult.Failure -> buildFailer.get().failBuild(result.message)
                }
            }
        }
    }

    /**
     * todo FileStorage needed only for ReportViewer
     */
    private fun getFileStorageUrl(): String {
        return reportViewerProperty.orNull?.fileStorageUrl ?: "http://stub"
    }
}
