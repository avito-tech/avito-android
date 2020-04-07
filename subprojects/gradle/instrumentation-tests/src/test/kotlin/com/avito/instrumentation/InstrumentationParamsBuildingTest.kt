package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.report.model.RunId
import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.ObjectInputStream
import java.nio.file.Path

class InstrumentationParamsBuildingTest {

    private lateinit var projectDir: File

    private val buildType: String = "buildType"
    private val targetBranch: String = "another"
    private lateinit var commit: String

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
                             output = project.file("outputs").path
                             reportApiUrl = "stub"
                             reportApiFallbackUrl = "stub"
                             reportViewerUrl = "stub"
                             sentryDsn = "stub"
                             slackToken = "stub"
                             fileStorageUrl = "stub"
                             registry = "stub"

                             instrumentationParams = [
                                 "jobSlug": "FunctionalTests",
                                 "override": "overrideInPlugin"
                             ]

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
                                                    device = Emulator22.INSTANCE
                                                    count = 1
                                                }
                                            }
                                        }
                                    }
                                 }

                                 dynamic {
                                    targets {
                                        disabled {
                                            deviceName = "disabled"

                                            enabled = false

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

                                        enabled {
                                            deviceName = "enabled"

                                            enabled = true

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
                             }
                         }
                    """.trimIndent()
                    )
                )
            ).generateIn(this)
        }

        with(projectDir) {
            git("branch $targetBranch")
            commit = git("rev-parse HEAD").trim()
        }
    }

    @Test
    fun `plugin instrumentation params has entries from android plugin configuration and environment override by root plugin configuration`() {
        val result = runGradle(
            "app:instrumentationDumpConfiguration"
        )
        result.assertThat().buildSuccessful()

        val data: GradleInstrumentationPluginConfiguration.Data = ObjectInputStream(
            projectDir
                .file(instrumentationDumpPath)
                .inputStream()
        )
            .use {
                it.readObject() as GradleInstrumentationPluginConfiguration.Data
            }


        val pluginConfigurationInstrumentationParameters = data.checkPluginLevelInstrumentationParameters()

        assertThat(pluginConfigurationInstrumentationParameters)
            .containsExactlyEntriesIn(
                mapOf(
                    "planSlug" to "AvitoAndroid",
                    "jobSlug" to "FunctionalTests",
                    "override" to "overrideInPlugin",
                    "deviceName" to "local",
                    "teamcityBuildId" to "0",
                    "buildBranch" to "develop",
                    "buildCommit" to commit,
                    "runId" to RunId(
                        commitHash = commit,
                        buildTypeId = "teamcity-$buildType"
                    ).toString(),
                    "reportApiUrl" to "stub",
                    "reportApiFallbackUrl" to "stub",
                    "reportViewerUrl" to "stub",
                    "sentryDsn" to "stub",
                    "slackToken" to "stub",
                    "fileStorageUrl" to "stub",
                    "inHouse" to "true",
                    "avito.report.enabled" to "false"
                )
            )
    }

    @Test
    fun `functional configuration instrumentation params has entries from plugin configuration override by functional configuration`() {
        val result = runGradle(
            "app:instrumentationDumpConfiguration"
        )
        result.assertThat().buildSuccessful()

        val data: GradleInstrumentationPluginConfiguration.Data = ObjectInputStream(
            projectDir
                .file(instrumentationDumpPath)
                .inputStream()
        )
            .use {
                it.readObject() as GradleInstrumentationPluginConfiguration.Data
            }

        val functionalConfigurationInstrumentationParameters = data.configurations
            .find { it.name == "functional" }!!
            .instrumentationParams

        assertThat(functionalConfigurationInstrumentationParameters)
            .containsExactlyEntriesIn(
                mapOf(
                    "planSlug" to "AvitoAndroid",
                    "jobSlug" to "FunctionalTests",
                    "override" to "overrideInConfiguration",
                    "configuration" to "functional",
                    "deviceName" to "local",
                    "teamcityBuildId" to "0",
                    "buildBranch" to "develop",
                    "buildCommit" to commit,
                    "runId" to RunId(
                        commitHash = commit,
                        buildTypeId = "teamcity-$buildType"
                    ).toString(),
                    "reportApiUrl" to "stub",
                    "reportApiFallbackUrl" to "stub",
                    "reportViewerUrl" to "stub",
                    "sentryDsn" to "stub",
                    "slackToken" to "stub",
                    "fileStorageUrl" to "stub",
                    "inHouse" to "true",
                    "avito.report.enabled" to "false"
                )
            )
    }

    @Test
    fun `target configuration instrumentation params has entries from instrumentation configuration override by target configuration`() {
        val result = runGradle(
            "app:instrumentationDumpConfiguration"
        )
        result.assertThat().buildSuccessful()

        val data: GradleInstrumentationPluginConfiguration.Data = ObjectInputStream(
            projectDir
                .file(instrumentationDumpPath)
                .inputStream()
        )
            .use {
                it.readObject() as GradleInstrumentationPluginConfiguration.Data
            }

        val api22TargetInstrumentationParameters = data.configurations
            .find { it.name == "functional" }!!
            .targets
            .find { it.name == "api22" }!!
            .instrumentationParams

        assertThat(api22TargetInstrumentationParameters)
            .containsExactlyEntriesIn(
                mapOf(
                    "deviceName" to "api22",
                    "planSlug" to "AvitoAndroid",
                    "jobSlug" to "FunctionalTests",
                    "target" to "yes",
                    "override" to "overrideInTarget",
                    "configuration" to "functional",
                    "teamcityBuildId" to "0",
                    "buildBranch" to "develop",
                    "buildCommit" to commit,
                    "runId" to RunId(
                        commitHash = commit,
                        buildTypeId = "teamcity-$buildType"
                    ).toString(),
                    "reportApiUrl" to "stub",
                    "reportApiFallbackUrl" to "stub",
                    "reportViewerUrl" to "stub",
                    "sentryDsn" to "stub",
                    "slackToken" to "stub",
                    "fileStorageUrl" to "stub",
                    "inHouse" to "true",
                    "avito.report.enabled" to "false"
                )
            )
    }

    @Test
    fun `target with enabled false hasn not passed through`() {
        val result = runGradle(
            "app:instrumentationDumpConfiguration"
        )
        result.assertThat().buildSuccessful()

        val data: GradleInstrumentationPluginConfiguration.Data = ObjectInputStream(
            projectDir
                .file(instrumentationDumpPath)
                .inputStream()
        )
            .use {
                it.readObject() as GradleInstrumentationPluginConfiguration.Data
            }

        val dynamicInstrumentationConfiguration = data.configurations
            .find { it.name == "dynamic" }!!

        assertThat(dynamicInstrumentationConfiguration.targets.find { it.name == "disabled" })
            .isNull()
        assertThat(dynamicInstrumentationConfiguration.targets.find { it.name == "enabled" })
            .isNotNull()
    }

    private fun runGradle(vararg args: String) =
        ciRun(
            projectDir,
            *args,
            "-PteamcityBuildId=0",
            buildType = buildType,
            targetBranch = targetBranch
        )
}
