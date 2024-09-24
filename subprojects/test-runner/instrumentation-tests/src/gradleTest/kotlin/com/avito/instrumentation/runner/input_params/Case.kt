package com.avito.instrumentation.runner.input_params

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.report.ReportConfig
import com.avito.instrumentation.internal.RunnerInputDumper
import com.avito.instrumentation.reservation.request.Device
import com.avito.runner.config.Reservation
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisMode
import com.avito.test.model.DeviceName
import com.avito.truth.isInstanceOf
import com.avito.utils.gradle.KubernetesCredentials
import com.google.common.truth.Truth
import org.junit.jupiter.api.DynamicTest
import java.io.File
import java.time.Duration

abstract class Case {
    abstract class Then(val expectedOutputDir: String) {
        abstract val expectedPluginInstrumentationParams: Map<String, String>
        val expectedVerdictPath: String = "$expectedOutputDir/verdict.json"
        abstract val expectedReportConfig: com.avito.runner.config.RunnerReportConfig
        val runnerInput: RunnerInputParams = RunnerInputDumper(File(expectedOutputDir)).readInput()
    }

    abstract val projectDir: File

    val buildType = "buildType"

    val targetBranch = "another"

    val appModuleName = "app"

    val appPackageName = "com.avito.app"

    val configurationName = "functional"

    abstract val reportConfig: ReportConfig
    abstract val buildScript: String

    fun assertions(commit: String): List<DynamicTest> {
        return assertions(createThen(commit))
    }

    protected abstract fun createThen(commit: String): Then

    private fun assertions(then: Then): List<DynamicTest> {
        return listOf(
            Assertion("mainApk path") {
                val mainApk = requireNotNull(it.mainApk) {
                    "main apk path must be not null"
                }
                Truth.assertThat(mainApk.canonicalPath)
                    .isEqualTo("${projectDir.canonicalPath}/$appModuleName/build/outputs/apk/debug/app-debug.apk")
            },
            Assertion("testApk path") {
                val testApk = requireNotNull(it.testApk) {
                    "test apk path must be not null"
                }
                Truth.assertThat(testApk.canonicalPath)
                    .isEqualTo(
                        "${projectDir.canonicalPath}/$appModuleName/" +
                            "build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
                    )
            },
            Assertion("instrumentation config name") {
                Truth.assertThat(it.instrumentationConfiguration.name)
                    .isEqualTo("functional")
            },
            Assertion("instrumentation params") {
                Truth.assertThat(it.instrumentationConfiguration.instrumentationParams)
                    .containsExactlyEntriesIn(then.expectedPluginInstrumentationParams)
            },
            Assertion("report skipped tests") {
                Truth.assertThat(it.instrumentationConfiguration.reportSkippedTests)
                    .isEqualTo(false)
            },
            Assertion("target device name") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].deviceName)
                    .isEqualTo(DeviceName("api22"))
            },
            Assertion("target name") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].name)
                    .isEqualTo("api22")
            },
            Assertion("target quota retry count") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].reservation.quota.retryCount)
                    .isEqualTo(0)
            },
            Assertion("target reservation quota min success count") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].reservation.quota.minimumSuccessCount)
                    .isEqualTo(1)
            },
            Assertion("target reservation quota min failed count") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].reservation.quota.minimumFailedCount)
                    .isEqualTo(0)
            },
            Assertion("target reservation device name") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].reservation.device.name)
                    .isEqualTo("27")
            },
            Assertion("target reservation device api") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].reservation.device.api)
                    .isEqualTo(27)
            },
            Assertion("target reservation device model") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].reservation.device.model)
                    .isEqualTo("Android_SDK_built_for_x86")
            },
            Assertion("target reservation device type") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].reservation.device)
                    .isInstanceOf<Device.LocalEmulator>()
            },
            Assertion("target reservation type") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].reservation)
                    .isInstanceOf<Reservation.StaticReservation>()
            },
            Assertion("target instrumentation params") {
                Truth.assertThat(it.instrumentationConfiguration.targets[0].instrumentationParams)
                    .containsExactlyEntriesIn(
                        then.expectedPluginInstrumentationParams.plus(
                            mapOf(
                                "target" to "yes",
                                "deviceName" to "api22",
                                "override" to "overrideInTarget",
                                "configuration" to "functional"
                            )
                        )
                    )
            },
            Assertion("instrumentation task timeout") {
                Truth.assertThat(it.instrumentationConfiguration.instrumentationTaskTimeout)
                    .isEqualTo(Duration.ofMinutes(120))
            },
            Assertion("test runner execution timeout") {
                Truth.assertThat(it.instrumentationConfiguration.testRunnerExecutionTimeout)
                    .isEqualTo(Duration.ofMinutes(100))
            },
            Assertion("instrumentation enableDeviceDebug") {
                Truth.assertThat(it.deviceDebug)
                    .isFalse()
            },
            Assertion("execution params app package") {
                Truth.assertThat(it.executionParameters.applicationPackageName)
                    .isEqualTo("$appPackageName.debug")
            },
            Assertion("execution params test app package") {
                Truth.assertThat(it.executionParameters.applicationTestPackageName)
                    .isEqualTo("$appPackageName.debug.test")
            },
            Assertion("execution params test runner") {
                Truth.assertThat(it.executionParameters.testRunner)
                    .isEqualTo("androidx.test.runner.AndroidJUnitRunner")
            },
            Assertion("execution params logcat tags") {
                Truth.assertThat(it.executionParameters.logcatTags)
                    .isEmpty()
            },
            Assertion("build id") {
                Truth.assertThat(it.buildId)
                    .isEqualTo("0")
            },
            Assertion("build type") {
                Truth.assertThat(it.buildType)
                    .isEqualTo("teamcity-buildType")
            },
            Assertion("kubernetes credentials is service") {
                Truth.assertThat(it.kubernetesCredentials)
                    .isInstanceOf<KubernetesCredentials.Service>()
            },
            Assertion("kubernetes credentials url") {
                Truth.assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).url)
                    .isEqualTo("myk8s.com")
            },
            Assertion("kubernetes credentials token") {
                Truth.assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).token)
                    .isEqualTo("q1w2e3")
            },
            Assertion("kubernetes credentials ca cert data") {
                Truth.assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).namespace)
                    .isEqualTo("default")
            },
            Assertion("project name") {
                Truth.assertThat(it.projectName)
                    .isEqualTo(appModuleName)
            },
            Assertion("suppress failure") {
                Truth.assertThat(it.suppressFailure)
                    .isTrue()
            },
            Assertion("suppress flaky") {
                Truth.assertThat(it.suppressFlaky)
                    .isTrue()
            },
            Assertion("impact analysis result runOnlyChangedTests flag") {
                Truth.assertThat(it.impactAnalysisResult.mode)
                    .isEqualTo(ImpactAnalysisMode.ALL)
            },
            Assertion("impact analysis result changed tests") {
                Truth.assertThat(it.impactAnalysisResult.changedTests)
                    .isEmpty()
            },
            Assertion("output dir") {
                Truth.assertThat(it.outputDir.canonicalPath)
                    .isEqualTo(then.expectedOutputDir)
            },
            Assertion("verdict file") {
                Truth.assertThat(it.verdictFile.canonicalPath)
                    .isEqualTo(
                        then.expectedVerdictPath
                    )
            },
            Assertion("statsd config is disabled") {
                Truth.assertThat(it.statsDConfig)
                    .isInstanceOf<StatsDConfig.Disabled>()
            },
            Assertion("report config") {
                Truth.assertThat(it.instrumentationConfiguration.reportConfig)
                    .isEqualTo(then.expectedReportConfig)
            },
            Assertion("proguard mappings is empty") {
                Truth.assertThat(it.proguardMappings)
                    .isEmpty()
            },
            Assertion("saveTestArtifactsToOutputs is disabled") {
                Truth.assertThat(it.saveTestArtifactsToOutputs)
                    .isFalse()
            },
            Assertion("useLegacyExtensionsV1Beta is disabled") {
                Truth.assertThat(it.useLegacyExtensionsV1Beta)
                    .isFalse()
            },
        ).map { assertion ->
            DynamicTest.dynamicTest("[kts] ${assertion.name}") {
                assertion.assert(then.runnerInput)
            }
        }
    }
}
