package com.avito.ci.steps

import com.avito.instrumentation.instrumentationPluginId
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.summary.testSummaryExtensionName
import com.avito.test.summary.testSummaryPluginId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TestSummaryStepTest {

    @Test
    fun testSummary(@TempDir projectDir: File) {
        generateProject(
            projectDir = projectDir,
            step = """
                |testSummary {
                |    configuration = "ui"
                |}
                |""".trimMargin()
        )

        ciRun(
            projectDir,
            "fullCheck",
            dryRun = true
        )
            .assertThat()
            .buildSuccessful().run {
                tasksShouldBeTriggered(
                    ":app1:instrumentationUi",
                    ":testSummarySomePlanSomeJob",
                    ":app1:fullCheck"
                ).inOrder()

                tasksShouldBeTriggered(
                    ":app2:instrumentationUi",
                    ":testSummarySomePlanSomeJob",
                    ":app2:fullCheck"
                ).inOrder()
            }
    }

    @Test
    fun flakyReport(@TempDir projectDir: File) {
        generateProject(
            projectDir = projectDir,
            step = """
                |flakyReport {
                |    configuration = "ui"
                |}
                |""".trimMargin()
        )

        ciRun(
            projectDir,
            "fullCheck",
            dryRun = true
        )
            .assertThat()
            .buildSuccessful().run {
                tasksShouldBeTriggered(
                    ":app1:instrumentationUi",
                    ":flakyReportSomePlanSomeJob",
                    ":app1:fullCheck"
                ).inOrder()

                tasksShouldBeTriggered(
                    ":app2:instrumentationUi",
                    ":flakyReportSomePlanSomeJob",
                    ":app2:fullCheck"
                ).inOrder()
            }
    }

    @Test
    fun markReportAsSourceForTMS(@TempDir projectDir: File) {
        generateProject(
            projectDir = projectDir,
            step = """
                |markReportAsSourceForTMS {
                |    configuration = "ui"
                |}
                |""".trimMargin()
        )

        ciRun(
            projectDir,
            "fullCheck",
            dryRun = true
        )
            .assertThat()
            .buildSuccessful().run {
                tasksShouldBeTriggered(
                    ":app1:instrumentationUi",
                    ":markReportForTmsSomePlanSomeJob",
                    ":app1:fullCheck"
                ).inOrder()

                tasksShouldBeTriggered(
                    ":app2:instrumentationUi",
                    ":markReportForTmsSomePlanSomeJob",
                    ":app2:fullCheck"
                ).inOrder()
            }
    }

    private fun generateProject(projectDir: File, step: String) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
                id(testSummaryPluginId)
            },
            buildGradleExtra = """
                |$testSummaryExtensionName {
                |   reportsHost = "http://stub"
                |   slackToken = "xxx"
                |   slackWorkspace = "xxx"
                |}
                |""".trimMargin(),
            modules = listOf(
                createAppModule("app1", step),
                createAppModule("app2", step)
            )
        ).generateIn(projectDir)
    }

    private fun createAppModule(name: String, step: String): AndroidAppModule {
        return AndroidAppModule(
            name = name,
            plugins = plugins {
                id(instrumentationPluginId)
                id("com.avito.android.cd")
            },
            imports = listOf(
                "import static com.avito.instrumentation.reservation.request.Device.LocalEmulator"
            ),
            buildGradleExtra = """
                        android {
                            defaultConfig {
                                testInstrumentationRunner = "no_matter"
                            }
                        }
                       
                        instrumentation {
                            sentryDsn = "stub"
                            
                            instrumentationParams = [
                                "planSlug" : "SomePlan",
                                "jobSlug"  : "SomeJob",
                            ]
                            
                            configurations {
                                ui {
                                    targets {
                                        api29 {
                                            deviceName = "api29"
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
                        
                        builds {
                            fullCheck {
                                $step
                            }
                        }
                    """.trimIndent()
        )
    }
}
