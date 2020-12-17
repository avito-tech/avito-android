@file:Suppress("UnstableApiUsage")

package com.avito.instrumentation

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.android.build.gradle.internal.tasks.ProguardConfigurableTask
import com.avito.android.InstrumentationChangedTestsFinderApi
import com.avito.android.LoadTestsFromApkTask
import com.avito.android.apkDirectory
import com.avito.android.changedTestsFinderTaskProvider
import com.avito.android.withAndroidApp
import com.avito.android.withAndroidLib
import com.avito.android.withAndroidModule
import com.avito.android.withArtifacts
import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.ImpactAnalysisPolicy.On
import com.avito.instrumentation.configuration.InstrumentationConfiguration.Data.DevicesType.CLOUD
import com.avito.instrumentation.configuration.createInstrumentationPluginExtension
import com.avito.instrumentation.configuration.withInstrumentationExtensionData
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.impact.analyzeTestImpactTask
import com.avito.instrumentation.test.DumpConfigurationTask
import com.avito.instrumentation.util.DelayTask
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.withType
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.envArgs
import com.avito.utils.gradle.kubernetesCredentials
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
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

        // see LintWorkerApiWorkaround.md
        project.tasks.register<DelayTask>(preInstrumentationTaskName) {
            group = ciTaskGroup
            description = "Executed when all inputs of all instrumentation tasks in the module are ready"

            delayMillis.set(MAGIC_DELAY)
        }

        project.withInstrumentationExtensionData { extensionData ->

            val loadTestsTask = project.tasks.register<LoadTestsFromApkTask>("loadTestsFromApk") {
                group = ciTaskGroup
                description = "Parse tests and it's annotation data from test apk dex file"
            }

            extensionData.configurations.forEach { instrumentationConfiguration ->
                if (instrumentationConfiguration.requestedDeviceType == CLOUD
                    && project.kubernetesCredentials is KubernetesCredentials.Empty
                ) {
                    throw IllegalStateException(
                        "Configuration ${instrumentationConfiguration.name} error: " +
                            "has kubernetes device target without kubernetes credentials"
                    )
                }

                val configurationOutputFolder = File(extensionData.output, instrumentationConfiguration.name)

                // see LintWorkerApiWorkaround.md
                val preInstrumentationTask = project.tasks.register<Task>(
                    preInstrumentationTaskName(instrumentationConfiguration.name)
                ) {
                    group = ciTaskGroup
                }

                if (instrumentationConfiguration.impactAnalysisPolicy is On.RunChangedTests) {
                    if (project.plugins.hasPlugin(InstrumentationChangedTestsFinderApi.pluginId)) {
                        project.tasks.changedTestsFinderTaskProvider().apply {
                            configure {
                                it.targetCommit.set(
                                    gitState.map { git ->
                                        requireNotNull(git.targetBranch?.commit) {
                                            "Target commit is required to find modified tests"
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                val instrumentationTaskProvider = project.tasks.register<InstrumentationTestsTask>(
                    instrumentationTaskName(instrumentationConfiguration.name)
                ) {
                    timeout.set(Duration.ofSeconds(instrumentationConfiguration.timeoutInSeconds))
                    group = ciTaskGroup

                    this.instrumentationConfiguration.set(instrumentationConfiguration)
                    this.buildId.set(env.build.id.toString())
                    this.buildType.set(env.build.type)
                    this.buildUrl.set(env.build.url)
                    this.gitBranch.set(gitState.map { it.currentBranch.name })
                    this.gitCommit.set(gitState.map { it.currentBranch.commit })
                    this.defaultBranch.set(gitState.map { it.defaultBranch })
                    this.sourceCommitHash.set(gitState.map { it.originalBranch.commit })
                    this.slackToken.set(extensionData.slackToken)
                    this.output.set(configurationOutputFolder)
                    if (extensionData.reportViewer != null) {
                        this.reportViewerConfig.set(extensionData.reportViewer)
                    }
                    this.registry.set(extensionData.registry)
                    this.kubernetesCredentials.set(project.kubernetesCredentials)

                    this.impactAnalysisPolicy.set(instrumentationConfiguration.impactAnalysisPolicy)

                    when (instrumentationConfiguration.impactAnalysisPolicy) {
                        is On.RunAffectedTests, is On.RunNewTests, is On.RunModifiedTests -> {
                            val impactTaskProvider = project.tasks.analyzeTestImpactTask()

                            this.dependencyOn(impactTaskProvider) {
                                this.affectedTests.set(it.testsToRunFile)
                                this.newTests.set(it.addedTestsFile)
                                this.modifiedTests.set(it.modifiedTestsFile)
                            }
                        }

                        is On.RunChangedTests -> {
                            if (project.plugins.hasPlugin(InstrumentationChangedTestsFinderApi.pluginId)) {
                                val impactTaskProvider = project.tasks.changedTestsFinderTaskProvider()

                                this.dependencyOn(impactTaskProvider) {
                                    this.changedTests.set(it.changedTestsFile)
                                }
                            }
                        }

                        is ImpactAnalysisPolicy.Off -> {
                        }
                    }
                }

                project.withAndroidLib { libExtension ->

                    val runner: String = libExtension.defaultConfig.getTestInstrumentationRunnerOrThrow()

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

                        loadTestsTask.configure {
                            if (extensionData.testApplicationApk == null) {
                                it.testApk.set(testApkProvider.get().apkDirectory())
                            } else {
                                it.testApk.set(File(extensionData.testApplicationApk))
                            }
                        }

                        preInstrumentationTask.configure { it.dependsOn(testApkProvider) }

                        instrumentationTaskProvider.configure { instrumentationTask ->
                            instrumentationTask.parameters.set(runFunctionalTestsParameters)

                            if (extensionData.testApplicationApk == null) {
                                instrumentationTask.dependencyOn(testApkProvider) { dependentTask ->
                                    instrumentationTask.testApplication.set(dependentTask.apkDirectory())
                                }
                            } else {
                                instrumentationTask.testApplication.set(File(extensionData.testApplicationApk))
                            }

                            if (extensionData.testProguardMapping == null) {
                                instrumentationTask.setupProguardMapping(
                                    instrumentationTask.testProguardMapping,
                                    testVariant
                                )
                            } else {
                                instrumentationTask.testProguardMapping.set(extensionData.testProguardMapping)
                            }
                        }
                    }
                }

                project.withAndroidApp { appExtension ->

                    appExtension.testVariants.all { testVariant: TestVariant ->
                        val testedVariant: ApplicationVariant = testVariant.testedVariant as ApplicationVariant

                        testVariant.withArtifacts { testVariantPackageTask, testedVariantPackageTask ->

                            val runner: String = appExtension.defaultConfig.getTestInstrumentationRunnerOrThrow()

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

                            instrumentationTaskProvider.configure { task ->
                                task.parameters.set(runFunctionalTestsParameters)

                                if (extensionData.applicationApk == null) {
                                    task.dependencyOn(testedVariantPackageTask) { dependentTask ->
                                        task.application.set(dependentTask.apkDirectory())
                                    }
                                } else {
                                    task.application.set(File(extensionData.applicationApk))
                                }

                                if (extensionData.testApplicationApk == null) {
                                    task.dependencyOn(testVariantPackageTask) { dependentTask ->
                                        task.testApplication.set(dependentTask.apkDirectory())
                                    }
                                } else {
                                    task.testApplication.set(File(extensionData.testApplicationApk))
                                }

                                if (extensionData.applicationProguardMapping == null) {
                                    task.setupProguardMapping(task.applicationProguardMapping, testedVariant)
                                } else {
                                    task.applicationProguardMapping.set(extensionData.applicationProguardMapping)
                                }

                                if (extensionData.testProguardMapping == null) {
                                    task.setupProguardMapping(task.testProguardMapping, testVariant)
                                } else {
                                    task.testProguardMapping.set(extensionData.testProguardMapping)
                                }
                            }
                        }
                    }
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
        args["teamcityBuildId"] = "-1"
        args["avito.report.enabled"] =
            project.getBooleanProperty("avito.report.enabled", default = false).toString()

        // TODO Need to be set before `afterEvaluate`
        config.testInstrumentationRunnerArguments(filterNotBlankValues(args))
    }

    private fun InstrumentationTestsTask.setupProguardMapping(
        mappingProperty: RegularFileProperty,
        variant: ApkVariant
    ) {
        project.tasks.withType<ProguardConfigurableTask>()
            .matching { it.variantName == variant.name }
            .firstOrNull()
            ?.let { proguardTask ->
                dependencyOn(proguardTask) { dependentTask ->
                    mappingProperty.set(dependentTask.mappingFile)
                }
            }
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

private fun DefaultConfig.getTestInstrumentationRunnerOrThrow(): String {
    val runner: String = requireNotNull(testInstrumentationRunner) {
        "testInstrumentationRunner must be set"
    }
    require(runner.isNotBlank()) {
        "testInstrumentationRunner must be set. Current value: $runner"
    }
    return runner
}

/**
 * Some empirical value that seems to solve project lock problem
 * see LintWorkerApiWorkaround.md
 */
private const val MAGIC_DELAY = 500L
