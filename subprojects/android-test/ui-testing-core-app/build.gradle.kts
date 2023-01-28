
import com.avito.instrumentation.configuration.KubernetesViaCredentials
import com.avito.instrumentation.reservation.request.Device
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import java.time.Duration

plugins {
    id("convention.kotlin-android-app")
    id("com.avito.android.instrumentation-tests")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "com.avito.android.test.app.core.TestAppRunner"

        val instrumentationArgs = mapOf(
            "planSlug" to "AndroidTestApp",
            "jobSlug" to "FunctionalTests",
            "fileStorageUrl" to (getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"),
            "teamcityBuildId" to (getOptionalStringProperty("teamcityBuildId") ?: "0"),
        )

        // These arguments are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments.putAll(instrumentationArgs)
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        unitTests {
            isIncludeAndroidResources = true

            all {
                val args = mapOf(
                    "android.junit.runner" to "com.avito.robolectric.runner.InHouseRobolectricTestRunner",
                    "planSlug" to "AndroidTestApp",
                    "jobSlug" to "FunctionalTests",
                )
                it.systemProperties.putAll(args)
            }
        }
    }

    packagingOptions {
        resources.pickFirsts.add("META-INF/okhttp.kotlin_module")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.playServicesBase)
    implementation(libs.recyclerView)
    implementation(libs.swipeRefreshLayout)

    implementation(projects.subprojects.androidLib.proxyToast)

    androidTestImplementation(projects.subprojects.testRunner.testInhouseRunner)
    androidTestImplementation(projects.subprojects.testRunner.testReportAndroid)
    androidTestImplementation(projects.subprojects.testRunner.testAnnotations)
    androidTestImplementation(projects.subprojects.testRunner.reportViewer)
    androidTestImplementation(projects.subprojects.androidTest.uiTestingCore)
    androidTestImplementation(projects.subprojects.androidTest.toastRule)
    androidTestImplementation(projects.subprojects.common.truthExtensions)

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

    environments {
        register<KubernetesViaCredentials>("kubernetes") {
            token.set(getMandatoryStringProperty("kubernetesToken"))
            caCertData.set(getMandatoryStringProperty("kubernetesCaCertData"))
            url.set(getMandatoryStringProperty("kubernetesUrl"))
            namespace.set(getMandatoryStringProperty("kubernetesNamespace"))
        }
    }

    experimental {
        useLegacyExtensionsV1Beta.set(false)
    }

    val defaultCpuRequest = "1.15"
    val defaultCpuLimit = "1.3"
    val defaultMemoryLimit = "4Gi"

    val emulator24 = Device.CloudEmulator(
        name = "api24",
        api = 24,
        model = "Android_SDK_built_for_x86",
        image = emulatorImage(24, "3a1f15409f37"),
        cpuCoresRequest = defaultCpuRequest,
        cpuCoresLimit = defaultCpuLimit,
        memoryLimit = defaultMemoryLimit
    )

    val emulator31 = Device.CloudEmulator(
        name = "api31",
        api = 31,
        model = "sdk_gphone64_x86_64",
        image = emulatorImage(31, "6a829b9c8932"),
        cpuCoresRequest = defaultCpuRequest,
        cpuCoresLimit = defaultCpuLimit,
        memoryLimit = "4.5Gi"
    )

    configurations {
        register("ui") {
            testRunnerExecutionTimeout = Duration.ofMinutes(10)
            instrumentationTaskTimeout = Duration.ofMinutes(10)
            reportSkippedTests = true
            filter = "ci"

            targets {
                register("api24") {
                    deviceName = "API24"

                    scheduling {
                        quota {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }

                        testsCountBasedReservation {
                            device = emulator24
                            minimum = 1
                            maximum = 10
                            testsPerEmulator = 12
                        }
                    }
                }

                register("api31") {
                    deviceName = "API31"

                    scheduling {
                        quota {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }
                        testsCountBasedReservation {
                            device = emulator31
                            minimum = 1
                            maximum = 10
                            testsPerEmulator = 12
                        }
                    }
                }
            }
        }
        register("local") {
            targets {
                register("api30") {
                    deviceName = "API30"
                    scheduling {
                        quota {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }
                        testsCountBasedReservation {
                            device = Device.LocalEmulator.device(30, "Android_SDK_built_for_x86_64")
                            maximum = 1
                            testsPerEmulator = 1
                        }
                    }
                }
            }
        }
    }
}

fun emulatorImage(api: Int, label: String): String {
    return if (avitoRegistry != null) {
        "$avitoRegistry/android/emulator-hermetic-$api:$label"
    } else {
        "avitotech/android-emulator-$api:$label"
    }
}

tasks.check {
    dependsOn(tasks.named("instrumentationUiKubernetes"))
}
