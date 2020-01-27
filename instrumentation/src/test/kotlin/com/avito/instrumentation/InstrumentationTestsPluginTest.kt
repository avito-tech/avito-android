package com.avito.instrumentation

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class InstrumentationTestsPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir temp: Path) {
        projectDir = temp.toFile()

        projectDir.apply {
            TestProjectGenerator(
                modules = listOf(
                    AndroidAppModule(
                        "app",
                        plugins = listOf("com.avito.android.instrumentation-tests"),
                        buildGradleExtra = """
                            import static com.avito.instrumentation.reservation.request.Device.Emulator.Emulator22

                            instrumentation {
                                output = project.file("outputs").path
                                reportApiUrl = "stub"
                                reportApiFallbackUrl = "stub"
                                reportViewerUrl = "stub"
                                sentryDsn = "stub"
                                slackToken = "stub"
                                fileStorageUrl = "stub"

                                instrumentationParams = [
                                    "jobSlug": "FunctionalTests"
                                ]

                                configurations {

                                    functional {
                                        instrumentationParams = [
                                            "deviceName": "api22"
                                        ]

                                        targets {
                                            api22 {
                                                deviceName = "api22"

                                                scheduling {
                                                    quota {
                                                        minimumSuccessCount = 1
                                                    }

                                                    staticDevicesReservation {
                                                        device = Emulator22.INSTANCE
                                                        count = 1
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    two {
                                        instrumentationParams = [
                                            "deviceName": "api22"
                                        ]

                                        targets {
                                            api22 {
                                                deviceName = "api22"

                                                scheduling {
                                                    quota {
                                                        minimumSuccessCount = 1
                                                    }

                                                    staticDevicesReservation {
                                                        device = Emulator22.INSTANCE
                                                        count = 1
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    three {

                                    }
                                }
                            }

                            android {
                                defaultConfig {
                                    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
                                    testInstrumentationRunnerArguments(["planSlug" : "AvitoAndroid"])
                                }
                            }
                    """.trimIndent()
                    )
                )
            ).generateIn(this)
        }
    }

    @Test
    fun `run instrumentation by name - first run`() {
        runGradle(":app:instrumentationTwo", "-PrunOnlyFailedTests=false").assertThat().run {
            tasksShouldBeTriggered(
                ":app:instrumentationTwo"
            ).inOrder()
        }
    }

    private fun runGradle(vararg args: String, dryRun: Boolean = true) =
        ciRun(
            projectDir, *args,
            "-PdeviceName=LOCAL",
            "-PteamcityBuildId=0",
            "-Papp.versionName=1",
            "-Papp.versionCode=1",
            "-Pavito.bitbucket.url=http://bitbucket",
            "-Pavito.bitbucket.projectKey=AA",
            "-Pavito.bitbucket.repositorySlug=android",
            "-Pavito.stats.enabled=false",
            "-Pavito.stats.host=http://stats",
            "-Pavito.stats.fallbackHost=http://stats",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=android",
            dryRun = dryRun
        )
}
