@file:Suppress("UnstableApiUsage")

package com.avito.instrumentation

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.avito.android.getApkFile
import com.avito.android.withAndroidApp
import com.avito.android.withAndroidModule
import com.avito.android.withArtifacts
import com.avito.git.Branch
import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.git.isOnDefaultBranch
import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.createInstrumentationPluginExtension
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.configuration.withInstrumentationExtensionData
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.rerun.BuildOnTargetCommitForTestTask
import com.avito.instrumentation.rerun.RunOnTargetBranchCondition
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.test.DumpConfigurationTask
import com.avito.instrumentation.util.DelayTask
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalIntProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.toOptional
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import com.avito.utils.gradle.envArgs
import com.avito.utils.gradle.kubernetesCredentials
import com.avito.utils.logging.ciLogger
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
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

        if (project.buildEnvironment !is BuildEnvironment.CI) {
            logger.info("Instrumentation plugin disabled due to non CI environment")
            return
        }
        val instrumentationConfigurations =
            project.extensions.getByType<InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration>()
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

        val targetBranch: Provider<Branch> = gitState.flatMap { state ->
            val targetBranch = state.targetBranch
            if (targetBranch != null) {
                Providers.of(targetBranch)
            } else {
                Providers.notDefined()
            }
        }

        project.withAndroidApp { appExtension ->

            // see LintWorkerApiWorkaround.md
            project.tasks.register<DelayTask>(preInstrumentationTaskName) {
                group = ciTaskGroup
                description =
                    "Executed when all inputs of all instrumentation tasks in the module are ready"

                delayMillis.set(500L)
            }

            appExtension.testVariants.all { testVariant: TestVariant ->
                val testedVariant: ApplicationVariant =
                    testVariant.testedVariant as ApplicationVariant

                val buildOnTargetCommitTask =
                    project.tasks.register<BuildOnTargetCommitForTestTask>("buildOnTargetCommit") {
                        group = ciTaskGroup
                        description =
                            "Run build on targetCommit to get apks for tests run on target branch"

                        val nestedBuildDir =
                            File(project.projectDir, "nested-build").apply { mkdirs() }
                        val variant = testedVariant.name
                        val versionName =
                            project.getMandatoryStringProperty("${project.name}.versionName")
                        val versionCode =
                            project.getMandatoryIntProperty("${project.name}.versionCode")

                        this.shouldFailBuild.set(false) //todo should be configurable outside
                        this.appPath.set(project.path)
                        this.testedVariant.set(variant)
                        this.targetCommit.set(targetBranch.map { it.commit })
                        this.tempDir.set(nestedBuildDir)
                        this.versionName.set(versionName)
                        this.versionCode.set(versionCode)
                        this.repoSshUrl.set(project.getMandatoryStringProperty("avito.repo.ssh.url"))
                        this.stubForTest.set(
                            project.getBooleanProperty(
                                "stubBuildOnTargetCommit",
                                default = false
                            )
                        )

                        onlyIf { targetBranch.isPresent }
                        onlyIf { !env.isRerunDisabled }

                        this.mainApk.set(testedVariant.packageApplicationProvider
                            .map { it.getApkFile() }
                            .map { it.relativeTo(project.rootDir) }
                            .map { RegularFile { nestedBuildDir.resolve(it) } })

                        this.testApk.set(testVariant.packageApplicationProvider
                            .map { it.getApkFile() }
                            .map { it.relativeTo(project.rootDir) }
                            .map { RegularFile { nestedBuildDir.resolve(it) } })
                    }

                project.withInstrumentationExtensionData { extensionData ->
                    extensionData.configurations.forEach { instrumentationConfiguration ->

                        logger.info("[InstrConfig:${instrumentationConfiguration.name}] Creating...")

                        testVariant.withArtifacts { testVariantPackageTask, testedVariantPackageTask ->

                            val configurationOutputFolder =
                                File(extensionData.output, instrumentationConfiguration.name)

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

                            val useArtifactsFromTargetBranch =
                                RunOnTargetBranchCondition.evaluate(instrumentationConfiguration)

                            if (useArtifactsFromTargetBranch is RunOnTargetBranchCondition.Result.Yes) {
                                logger.debug("[InstrConfig:${instrumentationConfiguration.name}] will depend on buildOnTargetBranch because: ${useArtifactsFromTargetBranch.reason}")
                            }

                            // see LintWorkerApiWorkaround.md
                            project.tasks.register<Task>(
                                preInstrumentationTaskName(instrumentationConfiguration.name)
                            ) {
                                group = ciTaskGroup

                                dependsOn(testedVariantPackageTask, testVariantPackageTask)

                                if (useArtifactsFromTargetBranch is RunOnTargetBranchCondition.Result.Yes) {
                                    dependsOn(buildOnTargetCommitTask)
                                }

                                if (instrumentationConfiguration.impactAnalysisPolicy is ImpactAnalysisPolicy.On) {
                                    dependsOn(
                                        instrumentationConfiguration.impactAnalysisPolicy.getTask(
                                            project
                                        )
                                    )
                                }
                            }

                            project.tasks.register<InstrumentationTestsTask>(
                                instrumentationTaskName(instrumentationConfiguration.name)
                            ) {
                                timeout.set(Duration.ofMinutes(100))
                                group = ciTaskGroup

                                if (extensionData.applicationApk == null) {
                                    dependencyOn(testedVariantPackageTask) { dependentTask ->
                                        application.set(dependentTask.getApkFile())
                                    }
                                } else {
                                    application.set(File(extensionData.applicationApk))
                                }

                                if (extensionData.testApplicationApk == null) {
                                    dependencyOn(testVariantPackageTask) { dependentTask ->
                                        testApplication.set(dependentTask.getApkFile())
                                    }
                                } else {
                                    testApplication.set(File(extensionData.testApplicationApk))
                                }

                                if (useArtifactsFromTargetBranch is RunOnTargetBranchCondition.Result.Yes) {
                                    dependencyOn(buildOnTargetCommitTask) { dependentTask ->
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
                                this.parameters.set(runFunctionalTestsParameters)
                                this.buildId.set(env.build.id.toString())
                                this.buildType.set(env.build.type)
                                this.buildUrl.set(env.build.url)
                                this.gitBranch.set(gitState.map { it.currentBranch.name })
                                this.gitCommit.set(gitState.map { it.currentBranch.commit })
                                this.targetBranch.set(targetBranch.map { it.name })
                                this.targetCommit.set(targetBranch.map { it.name })
                                this.defaultBranch.set(gitState.map { it.defaultBranch })
                                this.fullTestSuite.set(isFullTestSuite)
                                this.sourceCommitHash.set(gitState.map { it.originalBranch.commit })

                                // будет переписано из [UiTestCheck]
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
        configurationsContainer.register(
            "dynamic"
        ) { configuration ->
            configuration.annotatedWith = testFilter.annotatedWith
            configuration.tryToReRunOnTargetBranch = false
            configuration.reportSkippedTests = true

            configuration.rerunFailedTests = skipSucceedTestsFromPreviousRun
            configuration.keepFailedTestsFromReport = keepFailedTestsFromReport
            configuration.prefixFilter = prefixFilter

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
