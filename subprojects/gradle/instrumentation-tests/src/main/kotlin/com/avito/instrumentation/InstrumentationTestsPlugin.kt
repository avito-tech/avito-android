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
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.rerun.BuildOnTargetCommitForTestTask
import com.avito.instrumentation.rerun.RunOnTargetBranchCondition
import com.avito.instrumentation.test.DumpConfigurationTask
import com.avito.instrumentation.util.DelayTask
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.toOptional
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import com.avito.utils.gradle.envArgs
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.register
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

class InstrumentationTestsPlugin : Plugin<Project> {

    private val ciTaskGroup = "ci"

    override fun apply(project: Project) {
        val env = project.envArgs
        val pluginConfiguration = InstrumentationPluginConfiguration(project)
        val logger = project.ciLogger
        val gitState = project.gitState { logger.info(it) }

        applyTestTasks(
            project = project,
            pluginConfiguration = pluginConfiguration
        )

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
                description = "Executed when all inputs of all instrumentation tasks in the module are ready"

                delayMillis.set(500L)
            }

            appExtension.testVariants.all { testVariant: TestVariant ->
                val testedVariant: ApplicationVariant = testVariant.testedVariant as ApplicationVariant

                val buildOnTargetCommitTask =
                    project.tasks.register<BuildOnTargetCommitForTestTask>("buildOnTargetCommit") {
                        group = ciTaskGroup
                        description = "Run build on targetCommit to get apks for tests run on target branch"

                        val nestedBuildDir = File(project.projectDir, "nested-build").apply { mkdirs() }
                        val variant = testedVariant.name
                        val versionName = project.getMandatoryStringProperty("${project.name}.versionName")
                        val versionCode = project.getMandatoryIntProperty("${project.name}.versionCode")

                        this.shouldFailBuild.set(false) //todo should be configurable outside
                        this.appPath.set(project.path)
                        this.testedVariant.set(variant)
                        this.targetCommit.set(targetBranch.map { it.commit })
                        this.tempDir.set(nestedBuildDir)
                        this.versionName.set(versionName)
                        this.versionCode.set(versionCode)
                        this.repoSshUrl.set(project.getMandatoryStringProperty("avito.repo.ssh.url"))
                        this.stubForTest.set(project.getBooleanProperty("stubBuildOnTargetCommit", default = false))

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

                pluginConfiguration.withData { configurationData ->
                    configurationData.configurations.forEach { instrumentationConfiguration ->

                        logger.info("[InstrConfig:${instrumentationConfiguration.name}] Creating...")

                        testVariant.withArtifacts { testVariantPackageTask, testedVariantPackageTask ->

                            val configurationOutputFolder =
                                File(configurationData.output, instrumentationConfiguration.name)

                            val runner = appExtension.defaultConfig.testInstrumentationRunner
                            require(runner.isNotBlank()) { "testInstrumentationRunner must be set" }
                            val runFunctionalTestsParameters = ExecutionParameters(
                                applicationPackageName = testedVariant.applicationId,
                                applicationTestPackageName = testVariant.applicationId,
                                testRunner = runner,
                                namespace = instrumentationConfiguration.kubernetesNamespace,
                                logcatTags = configurationData.logcatTags
                            )

                            val useArtifactsFromTargetBranch =
                                RunOnTargetBranchCondition.evaluate(instrumentationConfiguration)

                            if (useArtifactsFromTargetBranch is RunOnTargetBranchCondition.Result.Yes) {
                                logger.info("[InstrConfig:${instrumentationConfiguration.name}] will depend on buildOnTargetBranch because: ${useArtifactsFromTargetBranch.reason}")
                            }

                            val preInstrumentationName = preInstrumentationTaskName(instrumentationConfiguration.name)

                            // see LintWorkerApiWorkaround.md
                            project.tasks.register<Task>(preInstrumentationName) {
                                group = ciTaskGroup

                                dependsOn(testedVariantPackageTask, testVariantPackageTask)

                                if (useArtifactsFromTargetBranch is RunOnTargetBranchCondition.Result.Yes) {
                                    dependsOn(buildOnTargetCommitTask)
                                }

                                when (instrumentationConfiguration.impactAnalysisPolicy) {
                                    is ImpactAnalysisPolicy.On -> {
                                        dependsOn(instrumentationConfiguration.impactAnalysisPolicy.getTask(project))
                                        logger.info("[InstrConfig:$preInstrumentationName] will depend on ui-impact-analysis")
                                    }
                                    is ImpactAnalysisPolicy.Off -> {
                                        logger.info("[InstrConfig:$preInstrumentationName] ui-impact-analysis is off")
                                    }
                                }
                            }

                            project.tasks.register<InstrumentationTestsTask>(
                                instrumentationTaskName(instrumentationConfiguration.name)
                            ) {
                                timeout.set(Duration.ofMinutes(100))
                                group = ciTaskGroup

                                dependencyOn(testedVariantPackageTask) { dependentTask ->
                                    application.set(dependentTask.getApkFile())
                                }

                                dependencyOn(testVariantPackageTask) { dependentTask ->
                                    testApplication.set(dependentTask.getApkFile())
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
                                            instrumentationConfiguration.impactAnalysisPolicy.getTask(project)
                                        ) {
                                            impactAnalysisResult.set(
                                                instrumentationConfiguration.impactAnalysisPolicy.getArtifact(it)
                                            )
                                        }

                                        logger.info("[InstrConfig:${instrumentationConfiguration.name}] will depend on ui-impact-analysis")
                                    }
                                    is ImpactAnalysisPolicy.Off -> {
                                        logger.info("[InstrConfig:${instrumentationConfiguration.name}] ui-impact-analysis is off")
                                    }
                                }

                                val isFullTestSuite = gitState.map {
                                        it.isOnDefaultBranch
                                            && instrumentationConfiguration.impactAnalysisPolicy is ImpactAnalysisPolicy.Off
                                    }
                                    .orElse(false)

                                this.instrumentationConfiguration.set(instrumentationConfiguration)
                                this.parameters.set(runFunctionalTestsParameters)
                                this.buildId.set(env.buildId)
                                this.buildUrl.set(env.buildUrl)
                                this.gitBranch.set(gitState.map { it.currentBranch.name })
                                this.gitCommit.set(gitState.map { it.currentBranch.commit })
                                this.targetBranch.set(targetBranch.map { it.name })
                                this.targetCommit.set(targetBranch.map { it.name })
                                this.defaultBranch.set(gitState.map { it.defaultBranch })
                                this.testedVariantName.set(testVariant.testedVariant.name)
                                this.fullTestSuite.set(isFullTestSuite)
                                this.sourceCommitHash.set(gitState.map { it.originalBranch.commit })

                                // будет переписано из [UiTestCheck]
                                this.sendStatistics.set(false)
                                this.slackToken.set(env.slackToken)

                                this.output.set(configurationOutputFolder)
                                this.reportApiUrl.set(configurationData.reportApiUrl)
                                this.reportApiFallbackUrl.set(configurationData.reportApiFallbackUrl)
                                this.reportViewerUrl.set(configurationData.reportViewerUrl)
                                this.registry.set(configurationData.registry)
                                this.unitToChannelMapping.set(configurationData.unitToChannelMapping)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupLocalInstrumentationArguments(project: Project, gitState: GitState?, config: DefaultConfig) {

        val args = mutableMapOf<String, String>()
        args["jobSlug"] = "LocalTests"
        args["runId"] = resolveLocalInstrumentationRunId()
        args["deviceName"] = project.getMandatoryStringProperty("deviceName")
        args["buildBranch"] = gitState?.currentBranch?.name ?: "local"
        args["buildCommit"] = gitState?.currentBranch?.commit ?: "local"
        args["teamcityBuildId"] = project.getMandatoryStringProperty("teamcityBuildId")

        config.testInstrumentationRunnerArguments(filterNotBlankValues(args))
    }

    /**
     * Здесь будут сосредоточены таски, которые используются ТОЛЬКО для интеграционного
     * тестирования плагина. Добавляем сюда их аккуратно только для критичных кейсов.
     */
    private fun applyTestTasks(project: Project, pluginConfiguration: InstrumentationPluginConfiguration) {
        pluginConfiguration.withData { configuration ->
            val configurationDumpFile = project.rootProject.file("instrumentation-configuration-dump.bin")

            project.tasks.register<DumpConfigurationTask>("instrumentationDumpConfiguration") {
                group = ciTaskGroup
                description = "Dump instrumentation configuration as json"

                this.configuration.set(configuration)
                this.output.set(configurationDumpFile)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun filterNotBlankValues(map: Map<String, Any?>) =
    map.filterValues { value: Any? -> value?.toString().isNullOrBlank().not() } as Map<String, String>

private fun resolveLocalInstrumentationRunId(): String =
    "LOCAL-${TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())}"
