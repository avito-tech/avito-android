package com.avito.ci.steps

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.summary.testSummaryExtensionName
import com.avito.test.summary.testSummaryPluginId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FlakyReportStepTest {

    @Test
    fun test(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = listOf(
                        testSummaryPluginId,
                        "com.avito.android.instrumentation-tests",
                        "com.avito.android.cd"
                    ),
                    buildGradleExtra = """
                        import static com.avito.instrumentation.reservation.request.Device.LocalEmulator
                        
                        android {
                            defaultConfig {
                                testInstrumentationRunner = "no_matter"
                            }
                        }
                        
                        $testSummaryExtensionName {
                            reportsHost = "stub"
                        }
                        
                        instrumentation {
                            sentryDsn = "stub"
                            slackToken = "stub"
                            registry = "stub"
                            
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
                                flakyReport {
                                    configuration = "ui"
                                }
                            }
                        }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        ciRun(
            projectDir,
            "fullCheck",
            dryRun = true
        )
            .assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(
                ":app:instrumentationUi",
                ":app:flakyReport",
                ":app:fullCheck"
            )
            .inOrder()
    }
}
