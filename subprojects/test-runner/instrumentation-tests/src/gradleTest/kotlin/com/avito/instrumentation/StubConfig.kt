package com.avito.instrumentation

import com.avito.instrumentation.configuration.report.ReportConfig

internal fun kotlinStubConfig(reportConfig: ReportConfig) = """
  |android {
  |    defaultConfig {
  |        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  |        testInstrumentationRunnerArguments(
  |            mapOf(
  |                "override" to "createdInInstrumentationRunnerArguments",
  |                "expectedCustomParam" to "value"
  |            )
  |        )
  |    }
  |}
  |
  |instrumentation {
  |  
  |    outputDir.set(rootProject.file("outputs"))
  |
  |    instrumentationParams = mapOf(
  |        "override" to "overrideInPlugin"
  |    )
  |    report.set(${
    when (reportConfig) {
        ReportConfig.NoOp -> "ReportConfig.NoOp"
        is ReportConfig.ReportViewer.SendFromDevice -> """
  |      ReportConfig.ReportViewer.SendFromDevice(  
  |         reportApiUrl = "${reportConfig.reportApiUrl}",
  |         reportViewerUrl = "${reportConfig.reportViewerUrl}",
  |         fileStorageUrl = "${reportConfig.fileStorageUrl}",
  |         planSlug = "${reportConfig.planSlug}",
  |         jobSlug = "${reportConfig.jobSlug}"
  |    )
    """.trimMargin()
        is ReportConfig.ReportViewer.SendFromRunner -> """
  |      ReportConfig.ReportViewer.SendFromRunner(  
  |         reportApiUrl = "${reportConfig.reportApiUrl}",
  |         reportViewerUrl = "${reportConfig.reportViewerUrl}",
  |         fileStorageUrl = "${reportConfig.fileStorageUrl}",
  |         planSlug = "${reportConfig.planSlug}",
  |         jobSlug = "${reportConfig.jobSlug}"
  |    )
    """.trimMargin()
    }
})
  |
  |    configurations {
  |        register("functional") {
  |            instrumentationParams = mapOf(
  |                "configuration" to "functional",
  |                "override" to "overrideInConfiguration"
  |            )
  |            jobSlug.set("override jobSlug")
  |            suppressFlaky.set(true)
  |            suppressFailure.set(true)
  |            
  |            targets {
  |                register("api22") {
  |                    instrumentationParams = mapOf(
  |                        "deviceName" to "invalid",
  |                        "target" to "yes",
  |                        "override" to "overrideInTarget"
  |                    )
  |
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
  |    }
  |    
  |    environments {
  |       register<com.avito.instrumentation.configuration.KubernetesViaContext>("k8sContext") {
  |         context.set("beta")
  |         namespace.set("default")
  |       }
  |       register<com.avito.instrumentation.configuration.KubernetesViaCredentials>("k8sCredentials") {
  |         token.set("q1w2e3")
  |         url.set("myk8s.com")
  |         namespace.set("default")
  |       }
  |    }
  |}
  |""".trimMargin()
