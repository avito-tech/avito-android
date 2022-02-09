package com.avito.instrumentation

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.internal.RunnerInputDumper
import com.avito.instrumentation.reservation.request.Device
import com.avito.reportviewer.model.RunId
import com.avito.runner.config.Reservation
import com.avito.runner.config.RunnerInputParams
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.git
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.model.DeviceName
import com.avito.truth.isInstanceOf
import com.avito.utils.gradle.KubernetesCredentials
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RunnerInputParamsTest {

    private val buildType = "buildType"

    private val targetBranch = "another"

    private val appModuleName = "app"

    private val appPackageName = "com.avito.app"

    @TestFactory
    fun `runner parameters - passed correctly - kotlin`(@TempDir projectDir: File): List<DynamicTest> {
        projectDir.apply {
            TestProjectGenerator(
                plugins = plugins {
                    id("com.avito.android.gradle-logger")
                },
                modules = listOf(
                    AndroidAppModule(
                        name = appModuleName,
                        packageName = appPackageName,
                        plugins = plugins {
                            id(instrumentationPluginId)
                        },
                        useKts = true,
                        imports = listOf(
                            "import com.avito.instrumentation.reservation.request.Device",
                        ),
                        buildGradleExtra = kotlinStubConfig
                    )
                ),
                useKts = true,
            ).generateIn(this)
        }

        return cases(projectDir, "kts")
    }

    @TestFactory
    fun `runner parameters - passed correctly - groovy`(@TempDir projectDir: File): List<DynamicTest> {
        projectDir.apply {
            TestProjectGenerator(
                plugins = plugins {
                    id("com.avito.android.gradle-logger")
                },
                modules = listOf(
                    AndroidAppModule(
                        name = appModuleName,
                        packageName = appPackageName,
                        plugins = plugins {
                            id(instrumentationPluginId)
                        },
                        imports = listOf(
                            "import static com.avito.instrumentation.reservation.request.Device.LocalEmulator",
                        ),
                        buildGradleExtra = groovyStubConfig
                    )
                )
            ).generateIn(this)
        }

        return cases(projectDir, "groovy")
    }

    private fun cases(
        projectDir: File,
        lang: String,
    ): List<DynamicTest> {

        with(projectDir) {
            git("branch $targetBranch")
        }

        val commit = projectDir.git("rev-parse HEAD").trim()

        val buildResult = ciRun(
            projectDir,
            "app:instrumentationFunctionalK8sCredentials",
            "-PteamcityBuildId=0",
            "-Pavito.git.state=env",
            "-PisGradleTestKitRun=true",
            buildType = buildType,
            targetBranch = targetBranch
        )

        buildResult.assertThat().buildSuccessful()

        val runId = RunId(
            prefix = "stub",
            identifier = commit,
            buildTypeId = "teamcity-$buildType"
        ).toReportViewerFormat()

        val configurationName = "functional"

        val expectedOutputDir = "${projectDir.canonicalPath}/outputs/stub.$commit.teamcity-buildType/functional"

        val runnerInput: RunnerInputParams = RunnerInputDumper(File(expectedOutputDir)).readInput()

        val expectedPluginInstrumentationParams = mapOf(
            "configuration" to configurationName,
            "planSlug" to "AppAndroid",
            "jobSlug" to "FunctionalTests",
            "override" to "overrideInConfiguration",
            "deviceName" to "local",
            "teamcityBuildId" to "0",
            "runId" to runId,
            "reportApiUrl" to "http://stub", // from InstrumentationPluginConfiguration
            "reportViewerUrl" to "http://stub",
            "fileStorageUrl" to "http://stub",
            "sentryDsn" to "stub",
            "avito.report.enabled" to "false", // todo
        )

        return listOf(
            Case("mainApk path") {
                val mainApk = requireNotNull(it.mainApk) {
                    "main apk path must be not null"
                }
                assertThat(mainApk.canonicalPath)
                    .isEqualTo("${projectDir.canonicalPath}/$appModuleName/build/outputs/apk/debug/app-debug.apk")
            },
            Case("testApk path") {
                val testApk = requireNotNull(it.testApk) {
                    "test apk path must be not null"
                }
                assertThat(testApk.canonicalPath)
                    .isEqualTo(
                        "${projectDir.canonicalPath}/$appModuleName/" +
                            "build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
                    )
            },
            Case("instrumentation config name") {
                assertThat(it.instrumentationConfiguration.name)
                    .isEqualTo("functional")
            },
            Case("instrumentation params") {
                assertThat(it.instrumentationConfiguration.instrumentationParams)
                    .containsExactlyEntriesIn(expectedPluginInstrumentationParams)
            },
            Case("report skipped tests") {
                assertThat(it.instrumentationConfiguration.reportSkippedTests)
                    .isEqualTo(false)
            },
            Case("target device name") {
                assertThat(it.instrumentationConfiguration.targets[0].deviceName)
                    .isEqualTo(DeviceName("api22"))
            },
            Case("target name") {
                assertThat(it.instrumentationConfiguration.targets[0].name)
                    .isEqualTo("api22")
            },
            Case("target quota retry count") {
                assertThat(it.instrumentationConfiguration.targets[0].reservation.quota.retryCount)
                    .isEqualTo(0)
            },
            Case("target reservation quota min success count") {
                assertThat(it.instrumentationConfiguration.targets[0].reservation.quota.minimumSuccessCount)
                    .isEqualTo(1)
            },
            Case("target reservation quota min failed count") {
                assertThat(it.instrumentationConfiguration.targets[0].reservation.quota.minimumFailedCount)
                    .isEqualTo(0)
            },
            Case("target reservation device name") {
                assertThat(it.instrumentationConfiguration.targets[0].reservation.device.name)
                    .isEqualTo("27")
            },
            Case("target reservation device api") {
                assertThat(it.instrumentationConfiguration.targets[0].reservation.device.api)
                    .isEqualTo(27)
            },
            Case("target reservation device model") {
                assertThat(it.instrumentationConfiguration.targets[0].reservation.device.model)
                    .isEqualTo("Android_SDK_built_for_x86")
            },
            Case("target reservation device type") {
                assertThat(it.instrumentationConfiguration.targets[0].reservation.device)
                    .isInstanceOf<Device.LocalEmulator>()
            },
            Case("target reservation type") {
                assertThat(it.instrumentationConfiguration.targets[0].reservation)
                    .isInstanceOf<Reservation.StaticReservation>()
            },
            Case("target instrumentation params") {
                assertThat(it.instrumentationConfiguration.targets[0].instrumentationParams)
                    .containsExactlyEntriesIn(
                        expectedPluginInstrumentationParams.plus(
                            mapOf(
                                "target" to "yes",
                                "deviceName" to "api22",
                                "override" to "overrideInTarget",
                                "configuration" to "functional"
                            )
                        )
                    )
            },
            Case("instrumentation enableDeviceDebug") {
                assertThat(it.deviceDebug)
                    .isFalse()
            },
            Case("execution params app package") {
                assertThat(it.executionParameters.applicationPackageName)
                    .isEqualTo("$appPackageName.debug")
            },
            Case("execution params test app package") {
                assertThat(it.executionParameters.applicationTestPackageName)
                    .isEqualTo("$appPackageName.debug.test")
            },
            Case("execution params test runner") {
                assertThat(it.executionParameters.testRunner)
                    .isEqualTo("androidx.test.runner.AndroidJUnitRunner")
            },
            Case("execution params logcat tags") {
                assertThat(it.executionParameters.logcatTags)
                    .isEmpty()
            },
            Case("build id") {
                assertThat(it.buildId)
                    .isEqualTo("0")
            },
            Case("build type") {
                assertThat(it.buildType)
                    .isEqualTo("teamcity-buildType")
            },
            Case("kubernetes credentials is service") {
                assertThat(it.kubernetesCredentials)
                    .isInstanceOf<KubernetesCredentials.Service>()
            },
            Case("kubernetes credentials url") {
                assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).url)
                    .isEqualTo("myk8s.com")
            },
            Case("kubernetes credentials token") {
                assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).token)
                    .isEqualTo("q1w2e3")
            },
            Case("kubernetes credentials ca cert data") {
                assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).caCertData)
                    .isEqualTo("12345")
            },
            Case("kubernetes credentials ca cert data") {
                assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).namespace)
                    .isEqualTo("default")
            },
            Case("project name") {
                assertThat(it.projectName)
                    .isEqualTo(appModuleName)
            },
            Case("suppress failure") {
                assertThat(it.suppressFailure)
                    .isTrue()
            },
            Case("suppress flaky") {
                assertThat(it.suppressFlaky)
                    .isTrue()
            },
            Case("impact analysis result runOnlyChangedTests flag") {
                assertThat(it.impactAnalysisResult.runOnlyChangedTests)
                    .isFalse()
            },
            Case("impact analysis result changed tests") {
                assertThat(it.impactAnalysisResult.changedTests)
                    .isEmpty()
            },
            Case("output dir") {
                assertThat(it.outputDir.canonicalPath)
                    .isEqualTo(expectedOutputDir)
            },
            Case("verdict file") {
                assertThat(it.verdictFile.canonicalPath)
                    .isEqualTo(
                        "${projectDir.canonicalPath}/outputs/stub.$commit.teamcity-buildType/functional/verdict.json"
                    )
            },
            Case("file storage url") {
                assertThat(it.fileStorageUrl)
                    .isEqualTo("http://stub")
            },
            Case("statsd config is disabled") {
                assertThat(it.statsDConfig)
                    .isInstanceOf<StatsDConfig.Disabled>()
            },
            Case("report api url") {
                assertThat(it.reportViewerConfig?.apiUrl)
                    .isEqualTo("http://stub")
            },
            Case("report viewer api url") {
                assertThat(it.reportViewerConfig?.viewerUrl)
                    .isEqualTo("http://stub")
            },
            Case("report viewer plan slug") {
                assertThat(it.reportViewerConfig?.reportCoordinates?.planSlug)
                    .isEqualTo("AppAndroid")
            },
            Case("report viewer job slug") {
                assertThat(it.reportViewerConfig?.reportCoordinates?.jobSlug)
                    .isEqualTo("FunctionalTests")
            },
            Case("report viewer run id") {
                assertThat(it.reportViewerConfig?.reportCoordinates?.runId)
                    .isEqualTo("stub.$commit.teamcity-buildType")
            },
            Case("proguard mappings is empty") {
                assertThat(it.proguardMappings)
                    .isEmpty()
            },
            Case("uploadTestArtifacts is disabled") {
                assertThat(it.uploadTestArtifacts)
                    .isFalse()
            },
            Case("fetchLogcatForIncompleteTests is disabled") {
                assertThat(it.fetchLogcatForIncompleteTests)
                    .isFalse()
            },
            Case("saveTestArtifactsToOutputs is disabled") {
                assertThat(it.saveTestArtifactsToOutputs)
                    .isFalse()
            },
            Case("useLegacyExtensionsV1Beta is disabled") {
                assertThat(it.useLegacyExtensionsV1Beta)
                    .isFalse()
            },
        ).map { case ->
            DynamicTest.dynamicTest("[$lang] ${case.name}") {
                case.assertion.invoke(runnerInput)
            }
        }
    }

    private data class Case(
        val name: String,
        val assertion: (RunnerInputParams) -> Unit
    )
}
