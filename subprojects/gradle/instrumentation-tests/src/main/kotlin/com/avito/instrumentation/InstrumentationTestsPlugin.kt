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
import com.avito.android.runner.devices.model.DeviceType.CLOUD
import com.avito.android.stats.statsdConfig
import com.avito.android.withAndroidApp
import com.avito.android.withAndroidLib
import com.avito.android.withAndroidModule
import com.avito.android.withArtifacts
import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.instrumentation.configuration.createInstrumentationPluginExtension
import com.avito.instrumentation.configuration.withInstrumentationExtensionData
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.test.DumpConfigurationTask
import com.avito.instrumentation.service.TestRunnerService
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.withType
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.envArgs
import com.avito.utils.gradle.kubernetesCredentials
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.kotlin.dsl.register
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

public class InstrumentationTestsPlugin : Plugin<Project> {

    private val ciTaskGroup = "ci"

    override fun apply(project: Project) {
        val env = project.envArgs
        val gitState = project.gitState()
        project.createInstrumentationPluginExtension()
        project.applyTestTasks()

        val uploadAllTestArtifacts = project.getBooleanProperty("avito.report.fromRunner", default = false)

        project.withAndroidModule { baseExtension ->
            setupLocalInstrumentationArguments(
                project = project,
                gitState = gitState.orNull,
                config = baseExtension.defaultConfig
            )
        }

        project.withInstrumentationExtensionData { extensionData ->

            val testRunnerServiceProvider = if (extensionData.useService) {
                project.gradle.sharedServices.registerIfAbsent(
                    "testRunnerService",
                    TestRunnerService::class.java
                ) { spec ->
                    with(spec.parameters) {
                        kubernetesCredentials.set(project.kubernetesCredentials)
                        statsDConfig.set(project.statsdConfig.get())
                        buildId.set(env.build.id.toString())
                        buildType.set(env.build.type)
                    }
                }
            } else {
                Providers.notDefined()
            }

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

                if (instrumentationConfiguration.runOnlyChangedTests) {
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
                    this.gitBranch.set(gitState.map { it.currentBranch.name })
                    this.gitCommit.set(gitState.map { it.currentBranch.commit })
                    this.testRunnerService.set(testRunnerServiceProvider)
                    this.output.set(configurationOutputFolder)
                    if (extensionData.reportViewer != null) {
                        this.reportViewerConfig.set(extensionData.reportViewer)
                    }
                    this.kubernetesCredentials.set(project.kubernetesCredentials)

                    this.runOnlyChangedTests.set(instrumentationConfiguration.runOnlyChangedTests)

                    this.uploadAllTestArtifacts.set(uploadAllTestArtifacts)

                    if (instrumentationConfiguration.runOnlyChangedTests) {
                        if (project.plugins.hasPlugin(InstrumentationChangedTestsFinderApi.pluginId)) {
                            val impactTaskProvider = project.tasks.changedTestsFinderTaskProvider()

                            this.dependencyOn(impactTaskProvider) {
                                this.changedTests.set(it.changedTestsFile)
                            }
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
