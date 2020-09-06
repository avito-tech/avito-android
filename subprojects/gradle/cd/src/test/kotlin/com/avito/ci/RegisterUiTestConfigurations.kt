package com.avito.ci

internal fun registerUiTestConfigurations(vararg names: String, isPerformance: Boolean = false): String {
    val configurations = names.map { name ->
        """$name {
            
            ${if (isPerformance) "performanceType = com.avito.instrumentation.configuration.InstrumentationConfiguration.PerformanceType.SIMPLE" else ""}
            
            targets {
                api22 {
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
        """.trimIndent()
    }
    return """
        import static com.avito.instrumentation.reservation.request.Device.LocalEmulator

        android.defaultConfig {
            testInstrumentationRunner = "no_matter"
            testInstrumentationRunnerArguments(["planSlug" : "AvitoAndroid"])
        }
        instrumentation {
            reportApiUrl = "stub"
            reportApiFallbackUrl = "stub"
            reportViewerUrl = "stub"
            registry = "stub"
            sentryDsn = "stub"
            slackToken = "stub"
            fileStorageUrl = "stub"
            instrumentationParams = [
                "deviceName"    : "regress",
                "jobSlug"       : "regress"
            ]

            output = "/"

            configurations {
                $configurations
            }
        }
    """.trimIndent()
}
