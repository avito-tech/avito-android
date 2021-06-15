package com.avito.instrumentation

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.internal.RunnerInputTester
import com.avito.instrumentation.reservation.request.Device
import com.avito.report.model.RunId
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.config.Reservation
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.git
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
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
                modules = listOf(
                    AndroidAppModule(
                        name = appModuleName,
                        packageName = appPackageName,
                        plugins = plugins {
                            id("com.avito.android.instrumentation-tests")
                        },
                        useKts = true,
                        imports = listOf(
                            "import com.avito.instrumentation.reservation.request.Device"
                        ),
                        buildGradleExtra = """
                         android {
                            defaultConfig {
                                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                                testInstrumentationRunnerArguments(
                                    mapOf(
                                        "planSlug" to "AvitoAndroid",
                                        "override" to "createdInInstrumentationRunnerArguments"
                                    )
                                )
                            }
                         }

                         instrumentation {
                             testDumpParams.set(true)
                             output = project.file("outputs").path
                             sentryDsn = "stub"

                             instrumentationParams = mapOf(
                                 "jobSlug" to "FunctionalTests",
                                 "override" to "overrideInPlugin"
                             )
                             
                             testReport {
                                reportViewer {
                                    reportApiUrl = "http://stub"
                                    reportViewerUrl = "http://stub"
                                    reportRunIdPrefix = "stub"
                                    fileStorageUrl = "http://stub"
                                }
                             }

                             configurations {

                                 register("functional") {
                                    instrumentationParams = mapOf(
                                        "configuration" to "functional",
                                        "override" to "overrideInConfiguration"
                                    )

                                    targets {
                                        register("api22") {
                                            instrumentationParams = mapOf(
                                                "deviceName" to "invalid",
                                                "target" to "yes",
                                                "override" to "overrideInTarget"
                                            )

                                            deviceName = "api22"

                                            scheduling {
                                                quota {
                                                    minimumSuccessCount = 1
                                                }

                                                staticDevicesReservation {
                                                    device = Device.LocalEmulator.device(27)
                                                    count = 1
                                                }
                                            }
                                        }
                                    }
                                 }
                             }
                         }
                    """.trimIndent()
                    )
                ),
                useKts = true,
            ).generateIn(this)
        }

        with(projectDir) {
            git("branch $targetBranch")
        }

        val commit = projectDir.git("rev-parse HEAD").trim()

        val buildResult = ciRun(
            projectDir,
            "app:instrumentationFunctional",
            "-PteamcityBuildId=0",
            "-Pavito.git.state=env",
            buildType = buildType,
            targetBranch = targetBranch
        )

        buildResult.assertThat().buildSuccessful()

        val runnerInput: InstrumentationTestsActionParams = RunnerInputTester.readInput(projectDir)

        return cases(projectDir, commit, runnerInput, "kts")
    }

    @TestFactory
    fun `runner parameters - passed correctly - groovy`(@TempDir projectDir: File): List<DynamicTest> {
        projectDir.apply {
            TestProjectGenerator(
                modules = listOf(
                    AndroidAppModule(
                        name = appModuleName,
                        packageName = appPackageName,
                        plugins = plugins {
                            id("com.avito.android.instrumentation-tests")
                        },
                        buildGradleExtra = """
                         import static com.avito.instrumentation.reservation.request.Device.LocalEmulator

                         android {
                            defaultConfig {
                                testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
                                testInstrumentationRunnerArguments([
                                    "planSlug" : "AvitoAndroid",
                                    "override": "createdInInstrumentationRunnerArguments"
                                ])
                            }
                         }

                         instrumentation {
                             testDumpParams = true
                             output = project.file("outputs").path
                             sentryDsn = "stub"

                             instrumentationParams = [
                                 "jobSlug": "FunctionalTests",
                                 "override": "overrideInPlugin"
                             ]
                             
                             testReport {
                                reportViewer {
                                    reportApiUrl = "http://stub"
                                    reportViewerUrl = "http://stub"
                                    reportRunIdPrefix = "stub"
                                    fileStorageUrl = "http://stub"
                                }
                             }

                             configurations {

                                 functional {
                                    instrumentationParams = [
                                        "configuration": "functional",
                                        "override": "overrideInConfiguration"
                                    ]

                                    targets {
                                        api22 {
                                            instrumentationParams = [
                                                "deviceName": "invalid",
                                                "target": "yes",
                                                "override": "overrideInTarget"
                                            ]

                                            deviceName = "api22"

                                            scheduling {
                                                quota {
                                                    minimumSuccessCount = 1
                                                }

                                                staticDevicesReservation {
                                                    device = LocalEmulator.device(27)
                                                    count = 1
                                                }
                                            }
                                        }
                                    }
                                 }
                             }
                         }
                    """.trimIndent()
                    )
                )
            ).generateIn(this)
        }

        with(projectDir) {
            git("branch $targetBranch")
        }

        val commit = projectDir.git("rev-parse HEAD").trim()

        val buildResult = ciRun(
            projectDir,
            "app:instrumentationFunctional",
            "-PteamcityBuildId=0",
            "-Pavito.git.state=env",
            buildType = buildType,
            targetBranch = targetBranch
        )

        buildResult.assertThat().buildSuccessful()

        val runnerInput: InstrumentationTestsActionParams = RunnerInputTester.readInput(projectDir)

        return cases(projectDir, commit, runnerInput, "groovy")
    }

    private fun cases(
        projectDir: File,
        commit: String,
        runnerInput: InstrumentationTestsActionParams,
        lang: String,
    ): List<DynamicTest> {

        val expectedPluginInstrumentationParams = mapOf(
            "configuration" to "functional",
            "planSlug" to "AvitoAndroid",
            "jobSlug" to "FunctionalTests",
            "override" to "overrideInConfiguration",
            "deviceName" to "local",
            "teamcityBuildId" to "0",
            "runId" to RunId(
                prefix = "stub",
                identifier = commit,
                buildTypeId = "teamcity-$buildType"
            ).toReportViewerFormat(),
            "reportApiUrl" to "http://stub", // from InstrumentationPluginConfiguration
            "reportViewerUrl" to "http://stub",
            "fileStorageUrl" to "http://stub",
            "sentryDsn" to "stub",
            "avito.report.enabled" to "false", // todo
        )

        return listOf(
            Case("mainApk path") {
                assertThat(it.mainApk?.path)
                    .isEqualTo("$projectDir/$appModuleName/build/outputs/apk/debug/app-debug.apk")
            },
            Case("testApk path") {
                assertThat(it.testApk.path)
                    .isEqualTo(
                        "$projectDir/$appModuleName/" +
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
            Case("kubernetes namespace") {
                // todo remove duplicate
                assertThat(it.instrumentationConfiguration.kubernetesNamespace)
                    .isEqualTo("default")
            },
            Case("target device name") {
                assertThat(it.instrumentationConfiguration.targets[0].deviceName)
                    .isEqualTo("api22")
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
                // todo remove duplicate
                assertThat(it.instrumentationConfiguration.enableDeviceDebug)
                    .isFalse()
            },
            Case("execution params app package") {
                assertThat(it.executionParameters.applicationPackageName)
                    .isEqualTo(appPackageName)
            },
            Case("execution params test app package") {
                assertThat(it.executionParameters.applicationTestPackageName)
                    .isEqualTo("$appPackageName.test")
            },
            Case("execution params test runner") {
                assertThat(it.executionParameters.testRunner)
                    .isEqualTo("androidx.test.runner.AndroidJUnitRunner")
            },
            Case("execution params test runner") {
                // todo remove duplicate
                assertThat(it.executionParameters.namespace)
                    .isEqualTo("default")
            },
            Case("execution params logcat tags") {
                assertThat(it.executionParameters.logcatTags)
                    .isEmpty()
            },
            Case("execution params enable device debug") {
                // todo remove duplicate
                assertThat(it.executionParameters.enableDeviceDebug)
                    .isFalse()
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
                    .isEqualTo("xxx")
            },
            Case("kubernetes credentials token") {
                assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).token)
                    .isEqualTo("xxx")
            },
            Case("kubernetes credentials ca cert data") {
                assertThat((it.kubernetesCredentials as KubernetesCredentials.Service).caCertData)
                    .isEqualTo("xxx")
            },
            Case("project name") {
                assertThat(it.projectName)
                    .isEqualTo(appModuleName)
            },
            Case("suppress failure") {
                assertThat(it.suppressFailure)
                    .isFalse()
            },
            Case("suppress flaky") {
                assertThat(it.suppressFlaky)
                    .isFalse()
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
                assertThat(it.outputDir.path)
                    .isEqualTo(
                        "$projectDir/$appModuleName/" +
                            "outputs/stub.$commit.teamcity-buildType/functional"
                    )
            },
            Case("verdict file") {
                assertThat(it.verdictFile.path)
                    .isEqualTo(
                        "$projectDir/$appModuleName/" +
                            "outputs/stub.$commit.teamcity-buildType/functional/verdict.json"
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
                    .isEqualTo("AvitoAndroid")
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
            Case("useInMemoryReport is disabled") {
                assertThat(it.useInMemoryReport)
                    .isFalse()
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
        ).map { case ->
            DynamicTest.dynamicTest("[$lang] ${case.name}") {
                case.assertion.invoke(runnerInput)
            }
        }
    }

    private data class Case(
        val name: String,
        val assertion: (InstrumentationTestsActionParams) -> Unit
    )
}
