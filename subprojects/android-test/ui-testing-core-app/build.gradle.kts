import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.KubernetesCredentials.Service
import com.avito.utils.gradle.kubernetesCredentials

plugins {
    id("convention.kotlin-android-app")
    id("com.avito.android.instrumentation-tests")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "com.avito.android.test.app.core.TestAppRunner"

        val instrumentationArgs = mapOf<String, String>(
            "planSlug" to "AndroidTestApp",
            "jobSlug" to "FunctionalTests",
//            "runId" to "local",
            "fileStorageUrl" to (getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"),
            "teamcityBuildId" to (getOptionalStringProperty("teamcityBuildId") ?: "0"),
        )

        // These arguments are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments(instrumentationArgs)
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    packagingOptions {
        pickFirst("META-INF/okhttp.kotlin_module")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.playServicesMaps)
    implementation(libs.recyclerView)

    implementation(projects.androidLib.proxyToast)

    androidTestImplementation(projects.testRunner.testInhouseRunner)
    androidTestImplementation(projects.testRunner.testReport)
    androidTestImplementation(projects.testRunner.testAnnotations)
    androidTestImplementation(projects.testRunner.reportViewer) {
        because("Priority/Behavior test annotations still there")
    }

    androidTestImplementation(projects.androidTest.uiTestingCore)
    androidTestImplementation(projects.androidTest.toastRule)
    androidTestImplementation(projects.common.truthExtensions)

    androidTestUtil(libs.testOrchestrator)
}

val avitoRegistry = getOptionalStringProperty("avito.registry")

instrumentation {

    testReport {
        reportViewer {
            reportApiUrl = getOptionalStringProperty("avito.report.url") ?: "http://stub"
            reportViewerUrl = getOptionalStringProperty("avito.report.viewerUrl") ?: "http://stub"
            fileStorageUrl = getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"
        }
    }

    sentryDsn = getOptionalStringProperty("avito.instrumentaion.sentry.dsn") ?: "http://stub-project@stub-host/0"

    logcatTags = setOf(
        "UITestRunner:*",
        "ActivityManager:*",
        "ReportTestListener:*",
        "StorageJsonTransport:*",
        "TestReport:*",
        "VideoCaptureListener:*",
        "TestRunner:*",
        "SystemDialogsManager:*",
        "AndroidJUnitRunner:*",
        "ito.android.de:*", // по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
        "*:E"
    )

    instrumentationParams = mapOf(
        "videoRecording" to "failed",
        "jobSlug" to "FunctionalTests"
    )

    filters.register("ci") {
        fromSource.excludeFlaky = true
    }

    val credentials = project.kubernetesCredentials
    if (credentials is Service || credentials is KubernetesCredentials.Config) {

        afterEvaluate {
            tasks.named("check").dependsOn(tasks.named("instrumentationUi"))
        }

        val emulator22 = CloudEmulator(
            name = "api22",
            api = 22,
            model = "Android_SDK_built_for_x86",
            image = emulatorImage(22, "7bb4b0b720"),
            cpuCoresRequest = "1",
            cpuCoresLimit = "1.3",
            memoryLimit = "4Gi"
        )

        val emulator29 = CloudEmulator(
            name = "api29",
            api = 29,
            model = "Android_SDK_built_for_x86_64",
            image = emulatorImage(29, "1927fb7cda"),
            cpuCoresRequest = "1",
            cpuCoresLimit = "1.3",
            memoryLimit = "4Gi"
        )

        configurations {
            register("ui") {
                kubernetesNamespace = "android-emulator"
                reportSkippedTests = true
                filter = "ci"

                targets {
                    register("api22") {
                        deviceName = "API22"

                        scheduling {
                            quota {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            testsCountBasedReservation {
                                device = emulator22
                                maximum = 50
                                minimum = 2
                                testsPerEmulator = 3
                            }
                        }
                    }

                    register("api29") {
                        deviceName = "API29"

                        scheduling {
                            quota {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            testsCountBasedReservation {
                                device = emulator29
                                maximum = 50
                                minimum = 2
                                testsPerEmulator = 3
                            }
                        }
                    }
                }
            }
        }
    }
}

fun emulatorImage(api: Int, label: String): String {
    return if (avitoRegistry != null) {
        "$avitoRegistry/android/emulator-$api:$label"
    } else {
        "avitotech/android-emulator-$api:$label"
    }
}
