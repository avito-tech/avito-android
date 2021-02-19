package com.avito.ci.steps

import com.avito.plugin.tmsPluginId
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class MarkReportAsSourceForTMSStepTest {

    @Test
    fun test(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(tmsPluginId)
                        id("com.avito.android.instrumentation-tests")
                        id("com.avito.android.cd")
                    },
                    buildGradleExtra = """
                        import static com.avito.instrumentation.reservation.request.Device.LocalEmulator
                        
                        android {
                            defaultConfig {
                                testInstrumentationRunner = "no_matter"
                            }
                        }
                        
                        tms {
                            reportsHost = "stub"
                        }
                        
                        instrumentation {
                            sentryDsn = "stub"
                            slackToken = "stub"
                            
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
                                markReportAsSourceForTMS {
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
                ":app:markReportAsSourceForTMSTask",
                ":app:fullCheck"
            )
            .inOrder()
    }
}
