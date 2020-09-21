@file:Suppress("UnstableApiUsage")

package com.avito.instrumentation

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.android.build.gradle.internal.tasks.ProguardConfigurableTask
import com.avito.android.getApkFile
import com.avito.android.withAndroidApp
import com.avito.android.withAndroidLib
import com.avito.android.withAndroidModule
import com.avito.android.withArtifacts
import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.createInstrumentationPluginExtension
import com.avito.instrumentation.configuration.withInstrumentationExtensionData
import com.avito.instrumentation.executing.ExecutionParameters
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

            delayMillis.set(500L)
        }

        project.withInstrumentationExtensionData { extensionData ->
            extensionData.configurations.forEach { instrumentationConfiguration ->
                if (!instrumentationConfiguration.isTargetLocalEmulators && project.kubernetesCredentials is KubernetesCredentials.Empty) {
                    throw IllegalStateException("Configuration ${instrumentationConfiguration.name} error: has kubernetes device target without kubernetes credentials")
                }

                val configurationOutputFolder = File(extensionData.output, instrumentationConfiguration.name)

                // see LintWorkerApiWorkaround.md
                val preInstrumentationTask = project.tasks.register<Task>(
                    preInstrumentationTaskName(instrumentationConfiguration.name)
                ) {
                    group = ciTaskGroup

                    if (instrumentationConfiguration.impactAnalysisPolicy is ImpactAnalysisPolicy.On) {
                        // todo implicit dependency on impact task
                        dependsOn(instrumentationConfiguration.impactAnalysisPolicy.getTask(project))
                    }
                }

                val instrumentationTask = project.tasks.register<InstrumentationTestsTask>(
                    instrumentationTaskName(instrumentationConfiguration.name)
                ) {
                    timeout.set(Duration.ofMinutes(100)) //todo move value to extension
                    group = ciTaskGroup

                    if (instrumentationConfiguration.impactAnalysisPolicy is ImpactAnalysisPolicy.On) {
                        dependencyOn(instrumentationConfiguration.impactAnalysisPolicy.getTask(project)) {
                            impactAnalysisPolicy.set(instrumentationConfiguration.impactAnalysisPolicy)
                            affectedTests.set(it.testsToRunFile)
                            newTests.set(it.addedTestsFile)
                            modifiedTests.set(it.modifiedTestsFile)
                        }
                    }

                    this.instrumentationConfiguration.set(instrumentationConfiguration)
                    this.buildId.set(env.build.id.toString())
                    this.buildType.set(env.build.type)
                    this.buildUrl.set(env.build.url)
                    this.gitBranch.set(gitState.map { it.currentBranch.name })
                    this.gitCommit.set(gitState.map { it.currentBranch.commit })
                    this.defaultBranch.set(gitState.map { it.defaultBranch })
                    this.sourceCommitHash.set(gitState.map { it.originalBranch.commit })

                    // will be changed in [UiTestCheck]
                    this.sendStatistics.set(false)
                    this.slackToken.set(extensionData.slackToken)
                    this.output.set(configurationOutputFolder)
                    if (extensionData.reportViewer != null) {
                        this.reportViewerConfig.set(extensionData.reportViewer)
                    }
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

                            if (extensionData.testProguardMapping == null) {
                                task.setupProguardMapping(task.testProguardMapping, testVariant)
                            } else {
                                task.testProguardMapping.set(extensionData.testProguardMapping)
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
