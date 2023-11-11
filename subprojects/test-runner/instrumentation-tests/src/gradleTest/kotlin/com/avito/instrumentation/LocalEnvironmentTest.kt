package com.avito.instrumentation

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class LocalEnvironmentTest {

    @Test
    fun `run local instrumentation by name - ok`(@TempDir projectDir: File) {
        val moduleName = "app"

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    moduleName,
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    imports = listOf(
                        "import com.avito.instrumentation.reservation.request.Device",
                        "import com.avito.instrumentation.configuration.report.ReportConfig",
                    ),
                    buildGradleExtra = instrumentationConfigurationWithCloudEmulator(),
                    useKts = true
                )
            ),
        ).generateIn(projectDir)

        ciRun(
            projectDir,
            ":$moduleName:instrumentationNonCloudLocal",
            dryRun = true
        ).assertThat()
            .run {
                tasksShouldBeTriggered(":$moduleName:instrumentationNonCloudLocal")
            }
    }

    private fun instrumentationConfigurationWithCloudEmulator(): String = """
    |instrumentation {
    |    outputDir.set(project.file("outputs"))
    |    report.set(ReportConfig.NoOp)
    |
    |    instrumentationParams = mapOf(
    |        "jobSlug" to "FunctionalTests"
    |    )
    |
    |    configurations {
    |
    |        register("nonCloud") {
    |            instrumentationParams = mapOf(
    |                "deviceName" to "api22"
    |            )
    |
    |            targets {
    |                register("api22") {
    |                    deviceName = "api22"
    |
    |                    scheduling {
    |                        quota {
    |                            minimumSuccessCount = 1
    |                        }
    |
    |                        staticDevicesReservation {
    |                            device = Device.LocalEmulator.device(27)
    |                            count = 1
    |                        }
    |                    }
    |                }
    |            }
    |        }
    |
    |        register("cloud") {
    |            instrumentationParams = mapOf(
    |                "deviceName" to "api29"
    |            )
    |
    |            targets {
    |                register("api29") {
    |                    deviceName = "api29"
    |
    |                    scheduling {
    |                        quota {
    |                            minimumSuccessCount = 1
    |                        }
    |
    |                        staticDevicesReservation {
    |                            device = Device.CloudEmulator(
    |                                name = "api29",
    |                                api = 29,
    |                                model = "Android_SDK_built_for_x86_64",
    |                                image = "avitotech/android-emulator-29:915c1f20be",
    |                            )
    |                            count = 1
    |                        }
    |                    }
    |                }
    |            }
    |        }
    |    }
    |}
    |
    |android {
    |    defaultConfig {
    |        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    |        testInstrumentationRunnerArguments(mapOf("planSlug" to "AvitoAndroid"))
    |    }
    |}
    |""".trimMargin()
}
