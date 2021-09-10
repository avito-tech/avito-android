package com.avito.instrumentation

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.tasks.ProguardConfigurableTask
import com.avito.android.InstrumentationChangedTestsFinderApi
import com.avito.android.apkDirectory
import com.avito.android.changedTestsFinderTaskProvider
import com.avito.android.runner.devices.model.DeviceType.CLOUD
import com.avito.android.withAndroidModule
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.StaticDeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.internal.AnalyticsResolver
import com.avito.instrumentation.internal.AndroidInstrumentationArgsDumper
import com.avito.instrumentation.internal.AndroidPluginInteractor
import com.avito.instrumentation.internal.BuildEnvResolver
import com.avito.instrumentation.internal.ExperimentsResolver
import com.avito.instrumentation.internal.GitResolver
import com.avito.instrumentation.internal.InstrumentationArgsResolver
import com.avito.instrumentation.internal.PlanSlugResolver
import com.avito.instrumentation.internal.ReportResolver
import com.avito.instrumentation.internal.RunIdResolver
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.withType
import com.avito.logger.GradleLoggerFactory
import com.avito.runner.config.InstrumentationConfigurationData
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.config.InstrumentationParameters
import com.avito.runner.config.Reservation
import com.avito.runner.config.SchedulingConfigurationData
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.suite.filter.Filter
import com.avito.test.model.DeviceName
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.kubernetesCredentials
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

@Suppress("UnstableApiUsage")
public class InstrumentationTestsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.createInstrumentationPluginExtension()
        val loggerFactory = GradleLoggerFactory.fromPlugin(this, project)
        val androidPluginInteractor = AndroidPluginInteractor(loggerFactory)
        val timeProvider: TimeProvider = DefaultTimeProvider()
        val runIdResolver = RunIdResolver(timeProvider, project)
        val reportResolver = ReportResolver(runIdResolver)
        val experimentsResolver = ExperimentsResolver(project)
        val instrumentationArgsResolver = InstrumentationArgsResolver(
            analyticsResolver = AnalyticsResolver,
            buildEnvResolver = BuildEnvResolver,
            reportResolver = reportResolver,
            planSlugResolver = PlanSlugResolver,
        )

        project.withAndroidModule { testedExtension ->

            val dumpDir = File(extension.output, dumpDirName).apply {
                mkdirs()
            }

            val gradleTestKitRun = project.getBooleanProperty("isGradleTestKitRun")

            if (gradleTestKitRun) {
                project.afterEvaluate {
                    val androidTestArgsTester = AndroidInstrumentationArgsDumper(dumpDir)
                    androidTestArgsTester.dumpArgs(testedExtension.defaultConfig.testInstrumentationRunnerArguments)
                }
            }

            extension.configurationsContainer.all { configuration ->

                var pluginLevelInstrumentationArgs: Map<String, String> = emptyMap()

                if (pluginLevelInstrumentationArgs.isEmpty()) {
                    pluginLevelInstrumentationArgs = instrumentationArgsResolver.resolvePluginLevelParams(
                        argsFromScript = androidPluginInteractor.getInstrumentationArgs(testedExtension),
                        project = project,
                        extension = extension,
                    )

                    androidPluginInteractor.addInstrumentationArgs(
                        testedExtension = testedExtension,
                        args = pluginLevelInstrumentationArgs
                    )
                }

                if (configuration.runOnlyChangedTests) {
                    setupChangedTests(project)
                }

                testedExtension.testVariants
                    .all { testVariant: @Suppress("DEPRECATION") com.android.build.gradle.api.TestVariant ->

                        val testedVariant = when (testedExtension) {
                            is AppExtension ->
                                testVariant.testedVariant as
                                    @Suppress("DEPRECATION") com.android.build.gradle.api.ApplicationVariant

                            is LibraryExtension -> testVariant

                            else -> throw RuntimeException(
                                "${testedExtension::class.java} not supported in InstrumentationPlugin"
                            )
                        }

                        project.tasks.register<InstrumentationTestsTask>(instrumentationTaskName(configuration.name)) {
                            timeout.set(configuration.instrumentationTaskTimeout)
                            group = CI_TASK_GROUP

                            this.parameters.set(
                                getExecutionParameters(
                                    testedVariant = testedVariant,
                                    testVariant = testVariant,
                                    extension = extension,
                                    configuration = configuration,
                                    runner = androidPluginInteractor.getTestInstrumentationRunnerOrThrow(
                                        testedExtension.defaultConfig
                                    ),
                                )
                            )

                            val reportViewer = reportResolver.getReportViewer(extension)

                            val instrumentationParameters = instrumentationArgsResolver.getInstrumentationParams(
                                extension = extension,
                                pluginLevelInstrumentationArgs = pluginLevelInstrumentationArgs
                            )

                            val outputFolder = File(
                                File(extension.output, reportResolver.getRunId(extension)),
                                configuration.name
                            )

                            this.instrumentationConfiguration.set(
                                getInstrumentationConfiguration(
                                    project = project,
                                    configuration = configuration,
                                    parentInstrumentationParameters = instrumentationParameters,
                                    filters = extension.filters.map { getInstrumentationFilter(it) },
                                    outputFolder = outputFolder,
                                )
                            )
                            this.buildId.set(BuildEnvResolver.getBuildId(project))
                            this.buildType.set(BuildEnvResolver.getBuildType(project))
                            this.experiments.set(experimentsResolver.getExperiments(extension))
                            this.gitBranch.set(GitResolver.getGitBranch(project))
                            this.gitCommit.set(GitResolver.getGitCommit(project))
                            this.output.set(outputFolder)

                            if (reportViewer != null) {
                                this.reportViewerProperty.set(reportViewer)
                            }
                            this.kubernetesCredentials.set(project.kubernetesCredentials)

                            val runOnlyChangedTests = configuration.runOnlyChangedTests

                            this.runOnlyChangedTests.set(runOnlyChangedTests)

                            if (runOnlyChangedTests) {
                                if (project.plugins.hasPlugin(InstrumentationChangedTestsFinderApi.pluginId)) {
                                    val impactTaskProvider = project.tasks.changedTestsFinderTaskProvider()

                                    this.dependencyOn(impactTaskProvider) {
                                        this.changedTests.set(it.changedTestsFile)
                                    }
                                }
                            }

                            this.gradleTestKitRun.set(gradleTestKitRun)

                            dependencyOn(testVariant.packageApplicationProvider) { task ->
                                testApplication.set(task.apkDirectory())
                            }

                            setupProguardMapping(
                                testProguardMapping,
                                testVariant
                            )

                            if (testedExtension is AppExtension) {
                                dependencyOn(testedVariant.packageApplicationProvider) { task ->
                                    application.set(task.apkDirectory())
                                }

                                setupProguardMapping(
                                    applicationProguardMapping,
                                    testedVariant
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun getExecutionParameters(
        testedVariant: @Suppress("DEPRECATION") com.android.build.gradle.api.ApkVariant,
        testVariant: @Suppress("DEPRECATION") com.android.build.gradle.api.TestVariant,
        extension: GradleInstrumentationPluginConfiguration,
        configuration: InstrumentationConfiguration,
        runner: String,
    ): ExecutionParameters {
        return ExecutionParameters(
            applicationPackageName = testedVariant.applicationId,
            applicationTestPackageName = testVariant.applicationId,
            testRunner = runner,
            namespace = configuration.kubernetesNamespace,
            logcatTags = extension.logcatTags,
            enableDeviceDebug = configuration.enableDeviceDebug
        )
    }

    private fun setupChangedTests(project: Project) {
        if (project.plugins.hasPlugin(InstrumentationChangedTestsFinderApi.pluginId)) {
            project.tasks.changedTestsFinderTaskProvider().apply {
                configure {
                    it.targetCommit.set(GitResolver.getTargetCommit(project))
                }
            }
        }
    }

    private fun InstrumentationTestsTask.setupProguardMapping(
        mappingProperty: RegularFileProperty,
        variant: @Suppress("DEPRECATION") com.android.build.gradle.api.ApkVariant
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

    private fun Project.createInstrumentationPluginExtension(): GradleInstrumentationPluginConfiguration {
        val extension =
            extensions.create<GradleInstrumentationPluginConfiguration>(
                "instrumentation",
                this
            )
        extension.filters.register("default") {
            it.fromRunHistory.excludePreviousStatuses(
                setOf(
                    InstrumentationFilter.FromRunHistory.RunStatus.Manual,
                    InstrumentationFilter.FromRunHistory.RunStatus.Success
                )
            )
        }
        return extension
    }

    private fun getInstrumentationConfiguration(
        project: Project,
        configuration: InstrumentationConfiguration,
        parentInstrumentationParameters: InstrumentationParameters,
        filters: List<InstrumentationFilterData>,
        outputFolder: File,
    ): InstrumentationConfigurationData {

        val mergedInstrumentationParameters: InstrumentationParameters =
            parentInstrumentationParameters
                .applyParameters(configuration.instrumentationParams)

        val result = InstrumentationConfigurationData(
            name = configuration.name,
            instrumentationParams = mergedInstrumentationParameters,
            reportSkippedTests = configuration.reportSkippedTests,
            kubernetesNamespace = configuration.kubernetesNamespace,
            targets = getTargets(configuration, mergedInstrumentationParameters),
            enableDeviceDebug = configuration.enableDeviceDebug,
            testRunnerExecutionTimeout = configuration.testRunnerExecutionTimeout,
            instrumentationTaskTimeout = configuration.instrumentationTaskTimeout,
            filter = filters.singleOrNull { it.name == configuration.filter }
                ?: throw IllegalStateException("Can't find filter=${configuration.filter}"),
            outputFolder = outputFolder
        )

        validate(result, project)

        return result
    }

    private fun validate(instrumentationConfigurationData: InstrumentationConfigurationData, project: Project) {
        if (instrumentationConfigurationData.requestedDeviceType == CLOUD
            && project.kubernetesCredentials is KubernetesCredentials.Empty
        ) {
            throw IllegalStateException(
                "Configuration ${instrumentationConfigurationData.name} error: " +
                    "has kubernetes device target without kubernetes credentials"
            )
        }
    }

    private fun getScheduling(schedulingConfiguration: SchedulingConfiguration): SchedulingConfigurationData {
        val currentReservation = schedulingConfiguration.reservation

        return SchedulingConfigurationData(
            reservation = when (currentReservation) {
                is StaticDeviceReservationConfiguration -> Reservation.StaticReservation(
                    device = currentReservation.device,
                    count = currentReservation.count,
                    quota = schedulingConfiguration.quota.data()
                )
                is TestsBasedDevicesReservationConfiguration -> Reservation.TestsCountBasedReservation(
                    device = currentReservation.device,
                    quota = schedulingConfiguration.quota.data(),
                    testsPerEmulator = currentReservation.testsPerEmulator!!,
                    maximum = currentReservation.maximum!!,
                    minimum = currentReservation.minimum
                )
                else -> throw RuntimeException("Unknown type of reservation")
            }
        )
    }

    private fun getTargetConfiguration(
        targetConfiguration: TargetConfiguration,
        parentInstrumentationParameters: InstrumentationParameters
    ): TargetConfigurationData {

        validate(targetConfiguration)

        val deviceName = targetConfiguration.deviceName

        require(deviceName.isNotBlank()) { "target.deviceName should be set" }

        return TargetConfigurationData(
            name = targetConfiguration.name,
            reservation = getScheduling(targetConfiguration.scheduling).reservation,
            deviceName = DeviceName(deviceName),
            instrumentationParams = parentInstrumentationParameters
                .applyParameters(targetConfiguration.instrumentationParams)
                .applyParameters(
                    mapOf("deviceName" to deviceName)
                )
        )
    }

    private fun validate(targetConfiguration: TargetConfiguration) {
        validate(targetConfiguration.scheduling)
    }

    private fun validate(schedulingConfiguration: SchedulingConfiguration) {
        schedulingConfiguration.reservation
        schedulingConfiguration.reservation.validate()
        schedulingConfiguration.quota
        schedulingConfiguration.quota.validate()
    }

    private fun getInstrumentationFilter(instrumentationFilter: InstrumentationFilter): InstrumentationFilterData {
        return InstrumentationFilterData(
            name = instrumentationFilter.name,
            fromSource = getFromSource(instrumentationFilter.fromSource),
            fromRunHistory = getFromRunHistory(instrumentationFilter.fromRunHistory)
        )
    }

    private fun getFromSource(fromSource: InstrumentationFilter.FromSource): InstrumentationFilterData.FromSource {
        return InstrumentationFilterData.FromSource(
            prefixes = fromSource.prefixes.value,
            annotations = fromSource.annotations.value,
            excludeFlaky = fromSource.excludeFlaky
        )
    }

    private fun getFromRunHistory(
        fromRunHistory: InstrumentationFilter.FromRunHistory
    ): InstrumentationFilterData.FromRunHistory {
        return InstrumentationFilterData.FromRunHistory(
            previousStatuses = Filter.Value(
                included = fromRunHistory.previous.value.included.map { it.map() }.toSet(),
                excluded = fromRunHistory.previous.value.excluded.map { it.map() }.toSet()
            ),
            reportFilter = fromRunHistory.reportFilter?.let { filter ->
                InstrumentationFilterData.FromRunHistory.ReportFilter(
                    statuses = Filter.Value(
                        included = filter.statuses.value.included.map { it.map() }.toSet(),
                        excluded = filter.statuses.value.excluded.map { it.map() }.toSet()
                    )
                )
            }
        )
    }

    private fun InstrumentationFilter.FromRunHistory.RunStatus.map(): com.avito.runner.config.RunStatus {
        return when (this) {
            InstrumentationFilter.FromRunHistory.RunStatus.Failed -> com.avito.runner.config.RunStatus.Failed
            InstrumentationFilter.FromRunHistory.RunStatus.Success -> com.avito.runner.config.RunStatus.Success
            InstrumentationFilter.FromRunHistory.RunStatus.Lost -> com.avito.runner.config.RunStatus.Lost
            InstrumentationFilter.FromRunHistory.RunStatus.Skipped -> com.avito.runner.config.RunStatus.Skipped
            InstrumentationFilter.FromRunHistory.RunStatus.Manual -> com.avito.runner.config.RunStatus.Manual
        }
    }

    private fun getTargets(
        configuration: InstrumentationConfiguration,
        parentInstrumentationParameters: InstrumentationParameters
    ): List<TargetConfigurationData> {
        val result = configuration.targetsContainer.toList()
            .filter { it.enabled }
            .map { getTargetConfiguration(it, parentInstrumentationParameters) }

        require(result.isNotEmpty()) {
            "configuration ${configuration.name} must have at least one target"
        }

        return result
    }
}

private const val CI_TASK_GROUP = "ci"
