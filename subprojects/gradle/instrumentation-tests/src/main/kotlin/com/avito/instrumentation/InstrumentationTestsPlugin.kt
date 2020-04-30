@file:Suppress("UnstableApiUsage")

package com.avito.instrumentation

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.avito.android.getApkFile
import com.avito.android.withAndroidApp
import com.avito.android.withAndroidLib
import com.avito.android.withAndroidModule
import com.avito.android.withArtifacts
import com.avito.buildontarget.buildOnTargetTask
import com.avito.buildontarget.hasBuildOnTargetPlugin
import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.git.isOnDefaultBranch
import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.createInstrumentationPluginExtension
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.configuration.withInstrumentationExtensionData
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.rerun.RunOnTargetCommitCondition
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.test.DumpConfigurationTask
import com.avito.instrumentation.util.DelayTask
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getOptionalIntProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.toOptional
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.envArgs
import com.avito.utils.gradle.kubernetesCredentials
import com.avito.utils.logging.ciLogger
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

class InstrumentationTestsPlugin : Plugin<Project> {

    private val ciTaskGroup = "ci"

    override fun apply(project: Project) {
        val env = project.envArgs
        val logger = project.ciLogger
        val gitState = project.gitState { logger.info(it) }
        project.createInstrumentationPluginExtension()
        project.applyTestTasks()

        project.withAndroidModule { baseExtension ->
            setupLocalInstrumentationArguments(
                project = project,
                gitState = gitState.orNull,
                config = baseExtension.defaultConfig
            )
        }

        val instrumentationConfigurations =
            project.extensions.getByType<InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration>()

        when (project.kubernetesCredentials) {
            !is KubernetesCredentials.Empty -> {
                instrumentationConfigurations.registerDynamicConfiguration(
                    testFilter = TestsFilter.valueOf(
                        project.getOptionalStringProperty(
                            "instrumentation.dynamic.testFilter",
                            TestsFilter.empty.name
                        )
                    ),
                    retryCountValue = project.getOptionalIntProperty(
                        "dynamicRetryCount",
                        1
                    ),
                    prefixFilter = project.getOptionalStringProperty("dynamicPrefixFilter", ""),
                    skipSucceedTestsFromPreviousRun = project.getBooleanProperty(
                        "instrumentation.dynamic.skipSucceedTestsFromPreviousRun",
                        true
                    ),
                    keepFailedTestsFromReport = project.getOptionalStringProperty("instrumentation.dynamic.keepFailedTestsFromReport"),
                    isDeviceEnabled = { device ->
                        project.getBooleanProperty(
                            "dynamicTarget${device.api}",
                            false
                        )
                    }
                )
            }
        }

        // see LintWorkerApiWorkaround.md
        project.tasks.register<DelayTask>(preInstrumentationTaskName) {
            group = ciTaskGroup
            description = "Executed when all inputs of all instrumentation tasks in the module are ready"

            delayMillis.set(500L)
        }

        project.withInstrumentationExtensionData { extensionData ->
            extensionData.configurations.forEach { instrumentationConfiguration ->
                if (!instrumentationConfiguration.isTargetLocalEmulators && project.kubernetesCredentials is KubernetesCredentials.Empty) {
                    throw IllegalStateException("Configuration ${instrumentationConfiguration.name} error: has kubernetes device target without kubernetes credentials")
                }
                val configurationOutputFolder =
                    File(extensionData.output, instrumentationConfiguration.name)

                val runOnTargetCommit = RunOnTargetCommitCondition.evaluate(
                    instrumentationConfiguration = instrumentationConfiguration,
                    hasBuildOnTargetPlugin = project.pluginManager.hasBuildOnTargetPlugin(),
                    buildOnTargetTaskProvider = { project.tasks.buildOnTargetTask() }
                )

                // see LintWorkerApiWorkaround.md
                val preInstrumentationTask = project.tasks.register<Task>(
                    preInstrumentationTaskName(instrumentationConfiguration.name)
                ) {
                    group = ciTaskGroup

                    if (runOnTargetCommit is RunOnTargetCommitCondition.Result.Yes) {
                        dependsOn(runOnTargetCommit.task)
                    }

                    if (instrumentationConfiguration.impactAnalysisPolicy is ImpactAnalysisPolicy.On) {
                        dependsOn(
                            instrumentationConfiguration.impactAnalysisPolicy.getTask(
                                project
                            )
                        )
                    }
                }

                val instrumentationTask = project.tasks.register<InstrumentationTestsTask>(
                    instrumentationTaskName(instrumentationConfiguration.name)
                ) {
                    timeout.set(Duration.ofMinutes(100)) //todo move value to extension
                    group = ciTaskGroup

                    if (runOnTargetCommit is RunOnTargetCommitCondition.Result.Yes) {
                        dependencyOn(runOnTargetCommit.task) { dependentTask ->
                            apkOnTargetCommit.set(dependentTask.mainApk.toOptional())
                            testApkOnTargetCommit.set(dependentTask.testApk.toOptional())
                        }
                    }

                    when (instrumentationConfiguration.impactAnalysisPolicy) {
                        is ImpactAnalysisPolicy.On -> {
                            dependencyOn(
                                instrumentationConfiguration.impactAnalysisPolicy.getTask(
                                    project
                                )
                            ) {
                                impactAnalysisResult.set(
                                    instrumentationConfiguration.impactAnalysisPolicy
                                        .getArtifact(it)
                                )
                            }
                        }
                    }

                    val isFullTestSuite = gitState.map {
                        it.isOnDefaultBranch
                                && instrumentationConfiguration.impactAnalysisPolicy is ImpactAnalysisPolicy.Off
                    }
                        .orElse(false)

                    this.instrumentationConfiguration.set(instrumentationConfiguration)
                    this.buildId.set(env.build.id.toString())
                    this.buildType.set(env.build.type)
                    this.buildUrl.set(env.build.url)
                    this.gitBranch.set(gitState.map { it.currentBranch.name })
                    this.gitCommit.set(gitState.map { it.currentBranch.commit })
                    this.defaultBranch.set(gitState.map { it.defaultBranch })
                    this.fullTestSuite.set(isFullTestSuite)
                    this.sourceCommitHash.set(gitState.map { it.originalBranch.commit })

                    // will be changed in [UiTestCheck]
                    this.sendStatistics.set(false)
                    this.slackToken.set(extensionData.slackToken)

                    this.output.set(configurationOutputFolder)
                    this.reportApiUrl.set(extensionData.reportApiUrl)
                    this.fileStorageUrl.set(extensionData.fileStorageUrl)
                    this.reportApiFallbackUrl.set(extensionData.reportApiFallbackUrl)
                    this.reportViewerUrl.set(extensionData.reportViewerUrl)
                    this.registry.set(extensionData.registry)
                    this.unitToChannelMapping.set(extensionData.unitToChannelMapping)
                    this.kubernetesCredentials.set(project.kubernetesCredentials)
                }

                project.withAndroidLib { libExtension ->

                    val runner = libExtension.defaultConfig.testInstrumentationRunner
                    require(runner.isNotBlank()) { "testInstrumentationRunner must be set" }

                    libExtension.testVariants.all { testVariant: TestVariant ->
                        val testApkProvider = testVariant.packageApplicationProvider

                        val runFunctionalTestsParameters = ExecutionParameters(
                            applicationPackageName = testVariant.applicationId,
                            applicationTestPackageName = testVariant.applicationId,
                            testRunner = runner,
                            namespace = instrumentationConfiguration.kubernetesNamespace,
                            logcatTags = extensionData.logcatTags,
                            enableDeviceDebug = instrumentationConfiguration.enableDeviceDebug
                        )

                        preInstrumentationTask.configure { it.dependsOn(testApkProvider) }

                        instrumentationTask.configure { task ->
                            task.parameters.set(runFunctionalTestsParameters)

                            if (extensionData.testApplicationApk == null) {
                                task.dependencyOn(testApkProvider) { dependentTask ->
                                    task.testApplication.set(dependentTask.getApkFile())
                                }
                            } else {
                                task.testApplication.set(File(extensionData.testApplicationApk))
                            }
                        }
                    }
                }

                project.withAndroidApp { appExtension ->

                    appExtension.testVariants.all { testVariant: TestVariant ->
                        val testedVariant: ApplicationVariant = testVariant.testedVariant as ApplicationVariant

                        testVariant.withArtifacts { testVariantPackageTask, testedVariantPackageTask ->

                            val runner = appExtension.defaultConfig.testInstrumentationRunner
                            require(runner.isNotBlank()) { "testInstrumentationRunner must be set" }

                            val runFunctionalTestsParameters = ExecutionParameters(
                                applicationPackageName = testedVariant.applicationId,
                                applicationTestPackageName = testVariant.applicationId,
                                testRunner = runner,
                                namespace = instrumentationConfiguration.kubernetesNamespace,
                                logcatTags = extensionData.logcatTags,
                                enableDeviceDebug = instrumentationConfiguration.enableDeviceDebug
                            )

                            preInstrumentationTask.configure {
                                it.dependsOn(
                                    testedVariantPackageTask,
                                    testVariantPackageTask
                                )
                            }

                            instrumentationTask.configure { task ->
                                task.parameters.set(runFunctionalTestsParameters)

                                if (extensionData.applicationApk == null) {
                                    task.dependencyOn(testedVariantPackageTask) { dependentTask ->
                                        task.application.set(dependentTask.getApkFile())
                                    }
                                } else {
                                    task.application.set(File(extensionData.applicationApk))
                                }

                                if (extensionData.testApplicationApk == null) {
                                    task.dependencyOn(testVariantPackageTask) { dependentTask ->
                                        task.testApplication.set(dependentTask.getApkFile())
                                    }
                                } else {
                                    task.testApplication.set(File(extensionData.testApplicationApk))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.registerDynamicConfiguration(
        testFilter: TestsFilter,
        retryCountValue: Int,
        prefixFilter: String,
        skipSucceedTestsFromPreviousRun: Boolean,
        keepFailedTestsFromReport: String?,
        isDeviceEnabled: (Device) -> Boolean
    ) {
        val filterName = "dynamic"
        filters.register(filterName) { filter ->
            filter.fromSource.includeByAnnotations(testFilter.annotatedWith)
            if (skipSucceedTestsFromPreviousRun) {
                filter.fromRunHistory.excludePreviousStatuses(
                    setOf(RunStatus.Success, RunStatus.Manual)
                )
            }
            if (keepFailedTestsFromReport != null) {
                filter.fromRunHistory.report(keepFailedTestsFromReport, filter = Action {
                    it.include(setOf(RunStatus.Failed, RunStatus.Lost))
                })
            }
            if (prefixFilter.isNotEmpty()) {
                filter.fromSource.includeByPrefixes(setOf(prefixFilter))
            }
        }
        configurationsContainer.register(
            "dynamic"
        ) { configuration ->
            configuration.tryToReRunOnTargetBranch = false
            configuration.reportSkippedTests = true
            configuration.filter = filterName
            configuration.targetsContainer.apply {
                EmulatorSet.full.forEach { emulator ->
                    register(
                        emulator.name,
                        Action { target ->
                            target.deviceName = "functional-${emulator.api}"
                            target.enabled = isDeviceEnabled(emulator)
                            target.scheduling = SchedulingConfiguration().apply {
                                quota = QuotaConfiguration().apply {
                                    retryCount = retryCountValue
                                    minimumSuccessCount =
                                        retryCountValue / 2 + retryCountValue % 2
                                    minimumFailedCount =
                                        retryCountValue / 2 + retryCountValue % 2
                                }

                                reservation =
                                    TestsBasedDevicesReservationConfiguration.create(
                                        device = emulator,
                                        min = 2,
                                        max = 25
                                    )
                            }
                        }
                    )
                }
            }
        }
    }

    // TODO: Make stronger contract: MBS-7890
    /**
     * NB: sync project in IDE after changes in InstrumentationRunnerArguments
     */
    private fun setupLocalInstrumentationArguments(
        project: Project,
        gitState: GitState?,
        config: DefaultConfig
    ) {

        val args = mutableMapOf<String, String>()
        args["jobSlug"] = "LocalTests"
        args["runId"] = resolveLocalInstrumentationRunId()
        args["deviceName"] = "local"
        args["buildBranch"] = gitState?.currentBranch?.name ?: "local"
        args["buildCommit"] = gitState?.currentBranch?.commit ?: "local"
        args["slackToken"] = "stub"
        args["sentryDsn"] = "http://stub-project@stub-host/0"
        args["fileStorageUrl"] = "http://stub"
        args["teamcityBuildId"] = project.envArgs.build.id.toString()
        args["avito.report.enabled"] =
            project.getBooleanProperty("avito.report.enabled", default = false).toString()

        config.testInstrumentationRunnerArguments(filterNotBlankValues(args))
    }

    /**
     * Здесь будут сосредоточены таски, которые используются ТОЛЬКО для интеграционного
     * тестирования плагина. Добавляем сюда их аккуратно только для критичных кейсов.
     */
    private fun Project.applyTestTasks() {
        withInstrumentationExtensionData { extension ->
            val configurationDumpFile =
                rootProject.file(instrumentationDumpPath)

            tasks.register<DumpConfigurationTask>("instrumentationDumpConfiguration") {
                group = ciTaskGroup
                description = "Dump instrumentation extension as json"

                this.configuration.set(extension)
                this.output.set(configurationDumpFile)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun filterNotBlankValues(map: Map<String, Any?>) =
    map.filterValues { value: Any? ->
        value?.toString().isNullOrBlank().not()
    } as Map<String, String>

private fun resolveLocalInstrumentationRunId(): String =
    "LOCAL-${TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())}"
