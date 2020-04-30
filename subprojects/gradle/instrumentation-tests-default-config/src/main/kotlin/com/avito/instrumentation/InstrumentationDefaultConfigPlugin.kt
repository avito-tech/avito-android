package com.avito.instrumentation

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.StaticDeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.reservation.request.Device.Emulator
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator24Cores2
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.kubernetesCredentials
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class InstrumentationDefaultConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        when (project.kubernetesCredentials) {
            // creds needed for emulators acquisition
            !is KubernetesCredentials.Empty -> {
                val performanceNamespace = project.getMandatoryStringProperty("performanceNamespace")
                val performanceMinimumSuccessCount = project.getMandatoryIntProperty("performanceMinimumSuccessCount")

                project.plugins.withType<InstrumentationTestsPlugin> {
                    project.extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

                        logcatTags = setOf(
                            "UITestRunner:*",
                            "ActivityManager:*",
                            "ReportTestListener:*",
                            "StorageJsonTransport:*",
                            "TestReport:*",
                            "VideoCaptureListener:*",
                            "TestRunner:*",
                            "SystemDialogsManager:*",
                            "ito.android.de:*", //по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
                            "*:E"
                        )

                        instrumentationParams = mapOf(
                            "videoRecording" to "failed",
                            "jobSlug" to "FunctionalTests"
                        )
                        filters.register("ui") {
                            it.fromSource.includeByAnnotations(TestsFilter.ui.annotatedWith)
                            it.fromRunHistory.excludePreviousStatuses(setOf(RunStatus.Success, RunStatus.Manual))
                        }
                        filters.register("uiNoE2e") {
                            it.fromSource.includeByAnnotations(TestsFilter.uiNoE2E.annotatedWith)
                            it.fromRunHistory.excludePreviousStatuses(setOf(RunStatus.Success, RunStatus.Manual))
                        }
                        filters.register("uiNoE2eNoFlaky") {
                            it.fromSource.includeByAnnotations(TestsFilter.uiNoE2E.annotatedWith)
                            it.fromSource.excludeByAnnotations(TestsFilter.flaky.annotatedWith)
                            it.fromRunHistory.excludePreviousStatuses(setOf(RunStatus.Success, RunStatus.Manual))
                        }
                        filters.register("newUi") {
                            it.fromSource.includeByAnnotations(TestsFilter.ui.annotatedWith)
                        }
                        filters.register("newUiNoE2E") {
                            it.fromSource.includeByAnnotations(TestsFilter.uiNoE2E.annotatedWith)
                        }
                        filters.register("regression") {
                            it.fromSource.includeByAnnotations(TestsFilter.regression.annotatedWith)
                            it.fromRunHistory.excludePreviousStatuses(setOf(RunStatus.Success, RunStatus.Manual))
                        }
                        filters.register("regressionNoE2E") {
                            it.fromSource.includeByAnnotations(TestsFilter.regressionNoE2E.annotatedWith)
                            it.fromRunHistory.excludePreviousStatuses(setOf(RunStatus.Success, RunStatus.Manual))
                        }
                        filters.register("performance") {
                            it.fromSource.includeByAnnotations(TestsFilter.performance.annotatedWith)
                        }
                        filters.register("performanceNoE2E") {
                            it.fromSource.includeByAnnotations(TestsFilter.performanceNoE2E.annotatedWith)
                        }
                        configurationsContainer.register(
                            "ui",
                            registerUiConfig("ui", hasE2eTests = true)
                        )
                        configurationsContainer.register(
                            "uiNoE2e",
                            registerUiConfig("uiNoE2e", hasE2eTests = false)
                        )
                        configurationsContainer.register(
                            "uiNoE2eNoFlaky",
                            registerUiConfig("uiNoE2eNoFlaky", hasE2eTests = false)
                        )

                        configurationsContainer.register(
                            "newUi",
                            registerNewUiConfig("newUi")
                        )
                        configurationsContainer.register(
                            "newUiNoE2e",
                            registerNewUiConfig("newUiNoE2E")
                        )

                        configurationsContainer.register(
                            "allUi",
                            registerAllUI("regression")
                        )
                        configurationsContainer.register(
                            "allUiNoE2e",
                            registerAllUI("regressionNoE2E")
                        )

                        configurationsContainer.register(
                            "regression",
                            registerRegressionConfig("regression")
                        )
                        configurationsContainer.register(
                            "regressionNoE2e",
                            registerRegressionConfig("regressionNoE2E")
                        )

                        //todo перенести в performance модуль?
                        configurationsContainer.register(
                            "performance", registerPerformanceConfig(
                                filterName = "performance",
                                k8sNamespace = performanceNamespace,
                                performanceMinimumSuccessCount = performanceMinimumSuccessCount,
                                performanceType = InstrumentationConfiguration.PerformanceType.SIMPLE
                            )
                        )
                        configurationsContainer.register(
                            "performanceNoE2e",
                            registerPerformanceConfig(
                                filterName = "performanceNoE2E",
                                k8sNamespace = performanceNamespace,
                                performanceMinimumSuccessCount = performanceMinimumSuccessCount,
                                performanceType = InstrumentationConfiguration.PerformanceType.SIMPLE
                            )
                        )
                        configurationsContainer.register(
                            "performanceMde", registerPerformanceConfig(
                                filterName = "performance",
                                k8sNamespace = performanceNamespace,
                                performanceMinimumSuccessCount = performanceMinimumSuccessCount,
                                performanceType = InstrumentationConfiguration.PerformanceType.MDE
                            )
                        )
                    }
                }
            }
        }
    }

    private fun registerUiConfig(
        filterName: String,
        hasE2eTests: Boolean
    ): Action<InstrumentationConfiguration> {

        fun NamedDomainObjectContainer<TargetConfiguration>.registerDevice(emulator: Emulator) {
            register("api${emulator.api}") { target ->
                target.deviceName = "API${emulator.api}"

                target.scheduling = SchedulingConfiguration().apply {
                    quota = QuotaConfiguration().apply {
                        retryCount = 3
                        minimumSuccessCount = 1
                    }

                    reservation = TestsBasedDevicesReservationConfiguration.create(
                        device = emulator,
                        min = 2,
                        max = 130
                    )
                }

                target.rerunScheduling = SchedulingConfiguration().apply {
                    quota = QuotaConfiguration().apply {
                        retryCount = 0
                        minimumFailedCount = 1
                    }

                    reservation = TestsBasedDevicesReservationConfiguration.create(
                        device = emulator,
                        min = 2,
                        max = 130
                    )
                }
            }
        }

        return Action { config ->
            config.tryToReRunOnTargetBranch = hasE2eTests
            config.reportSkippedTests = true
            config.reportFlakyTests = true
            config.impactAnalysisPolicy = ImpactAnalysisPolicy.On.RunAffectedTests
            config.filter = filterName
            EmulatorSet.fast.forEach { config.targetsContainer.registerDevice(it) }
        }
    }

    private fun registerPerformanceConfig(
        filterName: String,
        k8sNamespace: String,
        performanceMinimumSuccessCount: Int,
        performanceType: InstrumentationConfiguration.PerformanceType
    ) = Action<InstrumentationConfiguration> { config ->
        config.filter = filterName
        config.performanceType = performanceType

        config.kubernetesNamespace = k8sNamespace

        config.instrumentationParams = mapOf("jobSlug" to "PerformanceTests")

        config.targetsContainer.register("api24") { target ->
            target.deviceName = "API24"

            target.scheduling = SchedulingConfiguration().apply {
                quota = QuotaConfiguration().apply {
                    retryCount = performanceMinimumSuccessCount + 20
                    minimumSuccessCount = performanceMinimumSuccessCount
                }

                reservation = TestsBasedDevicesReservationConfiguration.create(
                    device = Emulator24Cores2,
                    min = 12,
                    max = 42,
                    testsPerEmulator = 1
                )
            }
        }
    }

    private fun registerNewUiConfig(
        filterName: String
    ): Action<InstrumentationConfiguration> {

        fun NamedDomainObjectContainer<TargetConfiguration>.registerDevice(emulator: Emulator) {
            register("api${emulator.api}") { target ->
                target.deviceName = "API${emulator.api}"

                target.scheduling = SchedulingConfiguration().apply {
                    quota = QuotaConfiguration().apply {
                        retryCount = 2
                        minimumSuccessCount = 3
                    }

                    reservation = TestsBasedDevicesReservationConfiguration.create(
                        device = emulator,
                        min = 2,
                        max = 30
                    )
                }
            }
        }

        return Action { config ->
            config.tryToReRunOnTargetBranch = false
            config.reportSkippedTests = false
            config.reportFlakyTests = true
            config.impactAnalysisPolicy = ImpactAnalysisPolicy.On.RunNewTests
            config.filter = filterName
            config.instrumentationParams = mapOf("jobSlug" to "NewFunctionalTests")

            EmulatorSet.fast.forEach { config.targetsContainer.registerDevice(it) }
        }
    }

    private fun registerAllUI(
        filterName: String
    ): Action<InstrumentationConfiguration> {

        fun NamedDomainObjectContainer<TargetConfiguration>.registerDevice(emulator: Emulator) {
            register("api${emulator.api}") { target ->
                target.deviceName = "API${emulator.api}"

                target.scheduling = SchedulingConfiguration().apply {
                    quota = QuotaConfiguration().apply {
                        retryCount = 3
                        minimumSuccessCount = 1
                    }

                    reservation = TestsBasedDevicesReservationConfiguration.create(
                        device = emulator,
                        min = 16,
                        max = 36
                    )
                }
            }
        }
        return Action { config ->
            config.tryToReRunOnTargetBranch = false
            config.reportSkippedTests = true
            config.filter = filterName
            EmulatorSet.fast.forEach { config.targetsContainer.registerDevice(it) }
        }
    }

    private fun registerRegressionConfig(filterName: String): Action<InstrumentationConfiguration> {

        fun NamedDomainObjectContainer<TargetConfiguration>.registerDevice(emulator: Emulator) =
            register(emulator.name) { target ->
                target.deviceName = "functional-${emulator.api}"

                target.scheduling = SchedulingConfiguration().apply {
                    quota = QuotaConfiguration().apply {
                        retryCount = 3
                        minimumSuccessCount = 1
                    }

                    reservation = StaticDeviceReservationConfiguration().apply {
                        device = emulator
                        count = 50
                    }
                }
            }

        return Action { config ->
            config.filter = filterName
            config.reportSkippedTests = true

            EmulatorSet.full.forEach { config.targetsContainer.registerDevice(it) }
        }
    }
}
