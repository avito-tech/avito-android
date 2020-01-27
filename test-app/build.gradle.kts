plugins {
    id("com.android.application")
    id("kotlin-android")
}

val kotlinVersion: String by project
val androidXVersion: String by project
val sentryVersion: String by project
val truthVersion: String by project
val okhttpVersion: String by project

//todo cleaner way to get these properties
val buildTools = requireNotNull(project.properties["buildToolsVersion"]).toString()
val compileSdk = requireNotNull(project.properties["compileSdkVersion"]).toString().toInt()
val targetSdk = requireNotNull(project.properties["targetSdkVersion"]).toString()
val minSdk = requireNotNull(project.properties["minSdkVersion"]).toString()

android {
    buildToolsVersion(buildTools)
    compileSdkVersion(compileSdk)

    defaultConfig {
        minSdkVersion(minSdk)
        targetSdkVersion(targetSdk)

        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "com.avito.android.ui.test.TestAppRunner"

        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "AndroidTestApp",
                "unnecessaryUrl" to "https://localhost"
            )
        )
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-maps:17.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("androidx.appcompat:appcompat:$androidXVersion")
    implementation("androidx.recyclerview:recyclerview:$androidXVersion")
    implementation("com.google.android.material:material:$androidXVersion")

    androidTestImplementation(project(":test-inhouse-runner")) { isTransitive = false }
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    androidTestImplementation(project(":test-report")) { isTransitive = false }
    androidTestImplementation(project(":ui-testing-core"))
    androidTestImplementation("io.sentry:sentry:$sentryVersion")
    androidTestImplementation("com.google.truth:truth:$truthVersion")
}

tasks.getByName("build").dependsOn("$path:assembleAndroidTest")

//instrumentation {
//
//    reportApiUrl = project.properties.get("avito.report.url")
//    reportApiFallbackUrl = project.properties.get("avito.report.fallbackUrl")
//    reportViewerUrl = project.properties.get("avito.report.viewerUrl")
//    registry = project.properties.get("avito.registry")
//
//    output = project.rootProject.file("outputs/$project.name/instrumentation").path
//
//    logcatTags = [
//            "TestAppRunner:*",
//            "InHouseInstrumentationTestRunner:*",
//            "ActivityManager:*",
//            "ReportTestListener:*",
//            "StorageJsonTransport:*",
//            "TestReport:*",
//            "VideoCaptureListener:*",
//            "TestRunner:*",
//            "SystemDialogsManager:*",
//            "*:E"
//    ]
//
//    instrumentationParams = [
//            "videoRecording": "failed",
//            "jobSlug"       : "FrameworkTests"
//    ]
//
//    configurations {
//        ui {
//
//            tryToReRunOnTargetBranch = false
//            reportSkippedTests = true
//            rerunFailedTests = true
//            reportFlakyTests = true
//
//            targets {
//                api22 {
//                    deviceName = "API22"
//
//                    scheduling {
//                        quota {
//                            retryCount = 1
//                            minimumSuccessCount = 1
//                        }
//
//                        testsCountBasedReservation {
//                            device = Emulator22.INSTANCE
//                            maximum = 50
//                            minimum = 2
//                            testsPerEmulator = 3
//                        }
//                    }
//                }
//
//                api27 {
//                    deviceName = "API27"
//
//                    scheduling {
//                        quota {
//                            retryCount = 1
//                            minimumSuccessCount = 1
//                        }
//
//                        testsCountBasedReservation {
//                            device = Emulator27.INSTANCE
//                            maximum = 50
//                            minimum = 2
//                            testsPerEmulator = 3
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

configurations.all {
    if (name.contains("AndroidTestRuntimeClasspath")) {
        resolutionStrategy {
            force("org.jetbrains:annotations:16.0.1")
        }
    }
}

//builds {
//    fastCheck {
//        uiTests {
//            configurations = ["ui"]
//            suppressFailures = false
//        }
//    }
//
//    fullCheck {
//        uiTests {
//            configurations = ["ui"]
//            suppressFailures = true
//        }
//    }
//}
