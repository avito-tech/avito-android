package com.avito.instrumentation

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.StaticDeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.reservation.request.Device.Emulator
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator22
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator23
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator24
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator24Cores2
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator27
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalIntProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

object EmulatorSet {
    val fast = setOf(Emulator22, Emulator27)
    val full = setOf(Emulator22, Emulator23, Emulator24, Emulator27)
}

object TestsFilter {

    private val manual = setOf("com.avito.android.test.annotations.ManualTest")

    val uiNoE2e = setOf(
        "com.avito.android.test.annotations.ComponentTest",
        "com.avito.android.test.annotations.InstrumentationUnitTest",
        "com.avito.android.test.annotations.PublishTest",
        "com.avito.android.test.annotations.MessengerTest",
        "com.avito.android.test.annotations.ScreenshotTest"
    )

    val ui = uiNoE2e + "com.avito.android.test.annotations.FunctionalTest"

    val regressionNoE2e = uiNoE2e + manual

    val regression = ui + manual

    val performanceNoE2e = setOf("com.avito.android.test.annotations.PerformanceComponentTest")

    val performance = performanceNoE2e + "com.avito.android.test.annotations.PerformanceFunctionalTest"
}

class InstrumentationDefaultConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val performanceNamespace = project.getMandatoryStringProperty("performanceNamespace")
        val performanceMinimumSuccessCount = project.getMandatoryIntProperty("performanceMinimumSuccessCount")

        project.plugins.withType<InstrumentationTestsPlugin> {
            project.extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

                output = project.rootProject.file("outputs/${project.name}/instrumentation").path

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

                configurationsContainer.register(
                    "dynamic",
                    registerDynamicConfig(
                        retryCountValue = project.getOptionalIntProperty(
                            "dynamicRetryCount",
                            1
                        ) * 2, // TODO почему * 2?
                        dynamicPrefixFilter = project.getOptionalStringProperty("dynamicPrefixFilter", ""),
                        isConfigEnabled = { apiVersion ->
                            project.getBooleanProperty(
                                "dynamicTarget$apiVersion",
                                false
                            )
                        }
                    )
                )

                configurationsContainer.register(
                    "ui",
                    registerUiConfig(TestsFilter.ui, hasE2eTests = true)
                )
                configurationsContainer.register(
                    "uiNoE2e",
                    registerUiConfig(TestsFilter.uiNoE2e, hasE2eTests = false)
                )

                configurationsContainer.register(
                    "newUi",
                    registerNewUiConfig(TestsFilter.ui)
                )
                configurationsContainer.register(
                    "newUiNoE2e",
                    registerNewUiConfig(TestsFilter.uiNoE2e)
                )

                configurationsContainer.register(
                    "allUi",
                    registerAllUI(TestsFilter.regression)
                )
                configurationsContainer.register(
                    "allUiNoE2e",
                    registerAllUI(TestsFilter.regressionNoE2e)
                )

                configurationsContainer.register(
                    "regression",
                    registerRegressionConfig(TestsFilter.regression)
                )
                configurationsContainer.register(
                    "regressionNoE2e",
                    registerRegressionConfig(TestsFilter.regressionNoE2e)
                )

                //todo перенести в performance модуль?
                configurationsContainer.register(
                    "performance", registerPerformanceConfig(
                        annotatedWith = TestsFilter.performance,
                        k8sNamespace = performanceNamespace,
                        performanceMinimumSuccessCount = performanceMinimumSuccessCount,
                        performanceType = InstrumentationConfiguration.PerformanceType.SIMPLE
                    )
                )
                configurationsContainer.register(
                    "performanceNoE2e",
                    registerPerformanceConfig(
                        annotatedWith = TestsFilter.performanceNoE2e,
                        k8sNamespace = performanceNamespace,
                        performanceMinimumSuccessCount = performanceMinimumSuccessCount,
                        performanceType = InstrumentationConfiguration.PerformanceType.SIMPLE
                    )
                )
                configurationsContainer.register(
                    "performanceMde", registerPerformanceConfig(
                        annotatedWith = TestsFilter.performance,
                        k8sNamespace = performanceNamespace,
                        performanceMinimumSuccessCount = performanceMinimumSuccessCount,
                        performanceType = InstrumentationConfiguration.PerformanceType.MDE
                    )
                )
            }
        }
    }

    private fun registerUiConfig(
        annotatedWith: Collection<String>,
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

                    reservation = testBasedReservation(
                        emulator = emulator,
                        min = 2,
                        max = 130
                    )
                }

                target.rerunScheduling = SchedulingConfiguration().apply {
                    quota = QuotaConfiguration().apply {
                        retryCount = 0
                        minimumFailedCount = 1
                    }

                    reservation = testBasedReservation(
                        emulator = emulator,
                        min = 2,
                        max = 130
                    )
                }
            }
        }

        return Action { config ->
            config.annotatedWith = annotatedWith
            config.tryToReRunOnTargetBranch = hasE2eTests
            config.reportSkippedTests = true
            config.rerunFailedTests = true
            config.reportFlakyTests = true
            config.impactAnalysisPolicy = ImpactAnalysisPolicy.On.RunAffectedTests

            EmulatorSet.fast.forEach { config.targetsContainer.registerDevice(it) }
        }
    }

    private fun registerPerformanceConfig(
        annotatedWith: Collection<String>,
        k8sNamespace: String,
        performanceMinimumSuccessCount: Int,
        performanceType: InstrumentationConfiguration.PerformanceType
    ) = Action<InstrumentationConfiguration> { config ->
        config.annotatedWith = annotatedWith
        config.rerunFailedTests = false

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

                reservation = testBasedReservation(
                    emulator = Emulator24Cores2,
                    min = 12,
                    max = 42,
                    testsPerEmulator = 1
                )
            }
        }
    }

    private fun registerNewUiConfig(annotatedWith: Collection<String>): Action<InstrumentationConfiguration> {

        fun NamedDomainObjectContainer<TargetConfiguration>.registerDevice(emulator: Emulator) {
            register("api${emulator.api}") { target ->
                target.deviceName = "API${emulator.api}"

                target.scheduling = SchedulingConfiguration().apply {
                    quota = QuotaConfiguration().apply {
                        retryCount = 2
                        minimumSuccessCount = 3
                    }

                    reservation = testBasedReservation(
                        emulator = emulator,
                        min = 2,
                        max = 30
                    )
                }
            }
        }

        return Action { config ->
            config.annotatedWith = annotatedWith
            config.tryToReRunOnTargetBranch = false
            config.reportSkippedTests = false
            config.reportFlakyTests = true
            config.rerunFailedTests = false
            config.impactAnalysisPolicy = ImpactAnalysisPolicy.On.RunNewTests

            config.instrumentationParams = mapOf("jobSlug" to "NewFunctionalTests")

            EmulatorSet.fast.forEach { config.targetsContainer.registerDevice(it) }
        }
    }

    private fun registerAllUI(annotatedWith: Collection<String>): Action<InstrumentationConfiguration> {

        fun NamedDomainObjectContainer<TargetConfiguration>.registerDevice(emulator: Emulator) {
            register("api${emulator.api}") { target ->
                target.deviceName = "API${emulator.api}"

                target.scheduling = SchedulingConfiguration().apply {
                    quota = QuotaConfiguration().apply {
                        retryCount = 3
                        minimumSuccessCount = 1
                    }

                    reservation = testBasedReservation(
                        emulator = emulator,
                        min = 16,
                        max = 36
                    )
                }
            }
        }
        return Action { config ->
            config.annotatedWith = annotatedWith
            config.tryToReRunOnTargetBranch = false
            config.rerunFailedTests = true
            config.reportSkippedTests = true

            EmulatorSet.fast.forEach { config.targetsContainer.registerDevice(it) }
        }
    }

    private fun registerDynamicConfig(
        retryCountValue: Int,
        dynamicPrefixFilter: String,
        isConfigEnabled: (apiVersion: Int) -> Boolean
    ) = Action<InstrumentationConfiguration> { config ->

        config.annotatedWith = TestsFilter.ui
        config.tryToReRunOnTargetBranch = false
        config.reportSkippedTests = true
        config.rerunFailedTests = false

        config.prefixFilter = dynamicPrefixFilter

        fun NamedDomainObjectContainer<TargetConfiguration>.registerDynamic(emulator: Emulator) =
            register(
                emulator.name,
                dynamicTarget(emulator, isConfigEnabled(emulator.api), retryCountValue)
            )

        EmulatorSet.full.forEach { config.targetsContainer.registerDynamic(it) }
    }

    private fun dynamicTarget(
        emulator: Emulator,
        isEnabled: Boolean,
        retryCountValue: Int
    ) = Action<TargetConfiguration> { target ->
        target.deviceName = "functional-${emulator.api}"

        target.enabled = isEnabled

        target.scheduling = SchedulingConfiguration().apply {
            quota = QuotaConfiguration().apply {
                retryCount = retryCountValue
                minimumSuccessCount = retryCountValue / 2
                minimumFailedCount = retryCountValue / 2
            }

            reservation = testBasedReservation(
                emulator = emulator,
                min = 2,
                max = 25
            )
        }
    }

    private fun registerRegressionConfig(annotatedWith: Collection<String>): Action<InstrumentationConfiguration> {

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
            config.annotatedWith = annotatedWith
            config.reportSkippedTests = true
            config.rerunFailedTests = true

            EmulatorSet.full.forEach { config.targetsContainer.registerDevice(it) }
        }
    }

    private fun testBasedReservation(
        emulator: Emulator,
        min: Int,
        max: Int,
        testsPerEmulator: Int = 12
    ): TestsBasedDevicesReservationConfiguration {
        return TestsBasedDevicesReservationConfiguration().apply {
            device = emulator
            maximum = max
            minimum = min
            this.testsPerEmulator = testsPerEmulator
        }
    }
}
