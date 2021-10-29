package com.avito.instrumentation

internal val kotlinStubConfig = """
  |android {
  |    defaultConfig {
  |        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  |        testInstrumentationRunnerArguments(
  |            mapOf(
  |                "planSlug" to "AppAndroid",
  |                "override" to "createdInInstrumentationRunnerArguments"
  |            )
  |        )
  |    }
  |}
  |
  |instrumentation {
  |  
  |    output = rootProject.file("outputs").path
  |    sentryDsn = "stub"
  |
  |    instrumentationParams = mapOf(
  |        "jobSlug" to "FunctionalTests",
  |        "override" to "overrideInPlugin"
  |    )
  |                             
  |    testReport {
  |        reportViewer {
  |            reportApiUrl = "http://stub"
  |            reportViewerUrl = "http://stub"
  |            reportRunIdPrefix = "stub"
  |            fileStorageUrl = "http://stub"
  |        }
  |    }
  |
  |    configurations {
  |        register("functional") {
  |            instrumentationParams = mapOf(
  |                "configuration" to "functional",
  |                "override" to "overrideInConfiguration"
  |            )
  |
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
  |         caCertData.set("12345")
  |         url.set("myk8s.com")
  |         namespace.set("default")
  |       }
  |    }
  |}
  |""".trimMargin()

internal val groovyStubConfig = """
  |android {
  |    defaultConfig {
  |        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
  |        testInstrumentationRunnerArguments([
  |            "planSlug" : "AppAndroid",
  |            "override": "createdInInstrumentationRunnerArguments"
  |        ])
  |    }
  |}
  |
  |instrumentation {
  |    output = rootProject.file("outputs").path
  |    sentryDsn = "stub"
  |
  |    instrumentationParams = [
  |        "jobSlug": "FunctionalTests",
  |        "override": "overrideInPlugin"
  |    ]
  |     
  |    testReport {
  |        reportViewer {
  |            reportApiUrl = "http://stub"
  |            reportViewerUrl = "http://stub"
  |            reportRunIdPrefix = "stub"
  |            fileStorageUrl = "http://stub"
  |        }
  |    }
  |
  |    configurations {
  |        functional {
  |            instrumentationParams = [
  |                "configuration": "functional",
  |                "override": "overrideInConfiguration"
  |            ]
  |            
  |            suppressFlaky.set(true)
  |            suppressFailure.set(true)
  |
  |            targets {
  |                api22 {
  |                    instrumentationParams = [
  |                        "deviceName": "invalid",
  |                        "target": "yes",
  |                        "override": "overrideInTarget"
  |                    ]
  |
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
  |    
  |    environments {
  |        register("k8sContext", com.avito.instrumentation.configuration.KubernetesViaContext) {
  |            context.set("beta")
  |            namespace.set("default")
  |        }
  |        register("k8sCredentials", com.avito.instrumentation.configuration.KubernetesViaCredentials) {
  |            token.set("q1w2e3")
  |            caCertData.set("12345")
  |            url.set("myk8s.com")
  |            namespace.set("default")
  |        }
  |    }
  |}
  |""".trimMargin()
