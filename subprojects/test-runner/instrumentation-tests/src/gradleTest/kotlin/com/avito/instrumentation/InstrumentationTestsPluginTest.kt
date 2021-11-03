package com.avito.instrumentation

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.Module
import com.avito.test.gradle.plugin.plugins
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class InstrumentationTestsPluginTest {

    @Test
    fun `configuration - ok - empty instrumentation block`(@TempDir projectDir: File) {
        createProject(
            projectDir = projectDir,
            module = AndroidAppModule(
                "app",
                plugins = plugins {
                    id(instrumentationPluginId)
                },
                buildGradleExtra = """
                    instrumentation {
                    }
                """.trimIndent()
            )
        )

        gradlew(projectDir, "help", dryRun = true).assertThat().buildSuccessful()
    }

    @Test
    fun `tasks resolution - ok - empty instrumentation block`(@TempDir projectDir: File) {
        createProject(
            projectDir = projectDir,
            module = AndroidAppModule(
                "app",
                plugins = plugins {
                    id(instrumentationPluginId)
                },
                buildGradleExtra = """
                    instrumentation {
                    }
                """.trimIndent()
            )
        )

        gradlew(projectDir, "tasks", dryRun = false).assertThat().buildSuccessful()
    }

    /**
     * IDE will turn red resolving script with plugin applied, it uses tasks or some equivalent in process
     *
     * todo Parameter: teamcityBuildId is required (must be digit)
     */
    @Disabled
    @Test
    fun `tasks resolution - ok - with configurations set and no args`(@TempDir projectDir: File) {
        createProject(
            projectDir = projectDir,
            module = AndroidAppModule(
                "app",
                plugins = plugins {
                    id(instrumentationPluginId)
                },
                buildGradleExtra = instrumentationConfiguration()
            )
        )

        gradlew(projectDir, "tasks", dryRun = false).assertThat().buildSuccessful()
    }

    @Test
    fun `run instrumentation by name - ok - in application project`(@TempDir projectDir: File) {
        val moduleName = "app"

        createProject(
            projectDir = projectDir,
            module = AndroidAppModule(
                moduleName,
                plugins = plugins {
                    id(instrumentationPluginId)
                },
                buildGradleExtra = instrumentationConfiguration()
            )
        )

        runGradle(projectDir, ":$moduleName:instrumentationTwoDefault", "-PrunOnlyFailedTests=false").assertThat()
            .run {
                tasksShouldBeTriggered(":$moduleName:instrumentationTwoDefault")
            }
    }

    @Test
    fun `run instrumentation by name - ok - in application project with flavors`(@TempDir projectDir: File) {
        val moduleName = "app"

        createProject(
            projectDir = projectDir,
            module = AndroidAppModule(
                moduleName,
                plugins = plugins {
                    id(instrumentationPluginId)
                },
                buildGradleExtra = """
                    |${instrumentationConfiguration()}
                    |    
                    |android {
                    |   flavorDimensions "version"
                    |    productFlavors {
                    |       demo { 
                    |           dimension "version"
                    |       }
                    |       full {
                    |           dimension "version"
                    |       }
                    |    }
                    |}
                    |""".trimMargin()
            )
        )

        runGradle(
            projectDir,
            ":$moduleName:instrumentationDemoTwoDefault",
            ":$moduleName:instrumentationFullTwoDefault",
            "-PrunOnlyFailedTests=false"
        ).assertThat()
            .run {
                tasksShouldBeTriggered(
                    ":$moduleName:instrumentationDemoTwoDefault",
                    ":$moduleName:instrumentationFullTwoDefault"
                ).inOrder()
            }
    }

    @Test
    fun `run instrumentation by name - ok - in application project with multidimensional flavors`(
        @TempDir projectDir: File
    ) {
        val moduleName = "app"

        createProject(
            projectDir = projectDir,
            module = AndroidAppModule(
                moduleName,
                plugins = plugins {
                    id(instrumentationPluginId)
                },
                buildGradleExtra = """
                    |${instrumentationConfiguration()}
                    |    
                    |android {
                    |   flavorDimensions "version", "monetization"
                    |    productFlavors {
                    |       demo { 
                    |           dimension "version"
                    |       }
                    |       full {
                    |           dimension "version"
                    |       }
                    |       free {
                    |           dimension "monetization"
                    |       }
                    |       paid {
                    |           dimension "monetization"
                    |       }
                    |    }
                    |}
                    |""".trimMargin()
            )
        )

        runGradle(
            projectDir,
            ":$moduleName:instrumentationDemoFreeTwoDefault",
            ":$moduleName:instrumentationFullPaidTwoDefault",
            "-PrunOnlyFailedTests=false"
        ).assertThat()
            .run {
                tasksShouldBeTriggered(
                    ":$moduleName:instrumentationDemoFreeTwoDefault",
                    ":$moduleName:instrumentationFullPaidTwoDefault"
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
                plugins = plugins {
                    id(instrumentationPluginId)
                },
                buildGradleExtra = instrumentationConfiguration()
            )
        )

        runGradle(projectDir, ":$moduleName:instrumentationTwoDefault", "-PrunOnlyFailedTests=false").assertThat()
            .run {
                tasksShouldBeTriggered(":$moduleName:instrumentationTwoDefault")
            }
    }

    private fun createProject(projectDir: File, module: Module) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(module)
        ).generateIn(projectDir)
    }

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
            dryRun = true
        )
}

@Language("groovy")
internal fun instrumentationConfiguration(): String = """
    |import static com.avito.instrumentation.reservation.request.Device.LocalEmulator
    |
    |instrumentation {
    |    output = project.file("outputs").path
    |    
    |    instrumentationParams = [
    |        "jobSlug": "FunctionalTests"
    |    ]
    |    
    |    experimental {
    |        useService = true
    |        useInMemoryReport = true
    |    }
    |
    |    configurations {
    |
    |        functional {
    |            instrumentationParams = [
    |                "deviceName": "api22"
    |            ]
    |
    |            targets {
    |                api22 {
    |                    deviceName = "api22"
    |
    |                    scheduling {
    |                        quota {
    |                            minimumSuccessCount = 1
    |                        }
    |
    |                        staticDevicesReservation {
    |                            device = LocalEmulator.device(27)
    |                            count = 1
    |                        }
    |                    }
    |                }
    |            }
    |        }
    |
    |        two {
    |            instrumentationParams = [
    |                "deviceName": "api22"
    |            ]
    |
    |            targets {
    |                api22 {
    |                    deviceName = "api22"
    |
    |                    scheduling {
    |                        quota {
    |                            minimumSuccessCount = 1
    |                        }
    |
    |                        staticDevicesReservation {
    |                            device = LocalEmulator.device(27)
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
    |        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    |        testInstrumentationRunnerArguments(["planSlug" : "AvitoAndroid"])
    |    }
    |}
    |""".trimMargin()
