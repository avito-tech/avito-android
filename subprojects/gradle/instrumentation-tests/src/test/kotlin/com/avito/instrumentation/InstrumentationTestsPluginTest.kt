package com.avito.instrumentation

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.Module
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class InstrumentationTestsPluginTest {

    @Test
    fun `run instrumentation by name - ok - in application project`(@TempDir projectDir: File) {
        val moduleName = "app"

        createProject(
            projectDir = projectDir,
            module = AndroidAppModule(
                moduleName,
                plugins = listOf("com.avito.android.instrumentation-tests"),
                buildGradleExtra = instrumentationConfiguration()
            )
        )

        runGradle(projectDir, ":$moduleName:instrumentationTwo", "-PrunOnlyFailedTests=false").assertThat()
            .run {
                tasksShouldBeTriggered(
                    ":$moduleName:instrumentationTwo"
                ).inOrder()
            }
    }

    @Test
    fun `run instrumentation by name - ok - in library project`(@TempDir projectDir: File) {
        val moduleName = "lib"

        createProject(
            projectDir = projectDir,
            module = AndroidLibModule(
                moduleName,
                plugins = listOf("com.avito.android.instrumentation-tests"),
                buildGradleExtra = instrumentationConfiguration()
            )
        )

        runGradle(projectDir, ":$moduleName:instrumentationTwo", "-PrunOnlyFailedTests=false").assertThat()
            .run {
                tasksShouldBeTriggered(
                    ":$moduleName:instrumentationTwo"
                ).inOrder()
            }
    }

    private fun createProject(projectDir: File, module: Module) {
        TestProjectGenerator(modules = listOf(module)).generateIn(projectDir)
    }

    private fun instrumentationConfiguration(): String = """
                        import static com.avito.instrumentation.reservation.request.Device.Emulator.Emulator22

                        instrumentation {
                            output = project.file("outputs").path
                            reportApiUrl = "stub"
                            reportApiFallbackUrl = "stub"
                            reportViewerUrl = "stub"
                            sentryDsn = "stub"
                            slackToken = "stub"
                            fileStorageUrl = "stub"
                            registry = "stub"

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

    private fun runGradle(projectDir: File, vararg args: String) =
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
            "-PkubernetesToken=stub",
            "-PkubernetesUrl=stub",
            "-PkubernetesCaCertData=stub",
            dryRun = true
        )
}
