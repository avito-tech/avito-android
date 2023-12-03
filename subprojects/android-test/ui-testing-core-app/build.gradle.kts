import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.KubernetesViaCredentials
import com.avito.instrumentation.configuration.report.ReportConfig
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
            "videoRecording" to "failed",
        )

        // These arguments are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments.putAll(instrumentationArgs)
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
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
    val reportKey = "avito.report.sender"
    val reportConfig = when (getOptionalStringProperty(reportKey, "noop")) {
        "noop" -> ReportConfig.NoOp
        "runner" -> ReportConfig.ReportViewer.SendFromRunner(
            reportApiUrl = getMandatoryStringProperty("avito.report.url"),
            reportViewerUrl = getMandatoryStringProperty("avito.report.viewerUrl"),
            fileStorageUrl = getMandatoryStringProperty("avito.fileStorage.url"),
            planSlug = "UiTestingCoreApp",
            jobSlug = "FunctionalTests"
        )
        else -> throw IllegalArgumentException("Invalid value for $reportKey property")
    }

    report.set(reportConfig)

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

    filters {
        register("ci") {
            fromSource.excludeFlaky = true
        }

        register("local") {
            fromSource
        }
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

    val emulator33 = Device.CloudEmulator(
        name = "api33",
        api = 33,
        model = "sdk_gphone_x86_64",
        image = emulatorImage(33, "409d2b4839b7"),
        cpuCoresRequest = defaultCpuRequest,
        cpuCoresLimit = defaultCpuLimit,
        memoryLimit = "4.5Gi"
    )

    configurations {
        register("local") {
            filter = "local"
            targets {
                register("api33") {
                    deviceName = "API33"
                    scheduling {
                        quota {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }
                        testsCountBasedReservation {
                            device = Device.LocalEmulator.device(33, "sdk_gphone_x86_64")
                            maximum = 1
                            testsPerEmulator = 1
                        }
                    }
                }
            }
        }

        register(
            "PRCheck",
            instrumentationConfiguration(
                targetDevices = setOf(emulator24, emulator33),
            )
        )
    }
}

fun instrumentationConfiguration(
    targetDevices: Set<Device>,
): Action<InstrumentationConfiguration> {
    return Action {
        testRunnerExecutionTimeout = Duration.ofMinutes(10)
        instrumentationTaskTimeout = Duration.ofMinutes(10)
        reportSkippedTests = true
        filter = "ci"

        targetDevices.forEach { targetDevice ->
            targets {
                register(targetDevice.name) {
                    scheduling {
                        deviceName = targetDevice.name

                        quota {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }

                        testsCountBasedReservation {
                            device = targetDevice
                            minimum = 1
                            maximum = 5
                            testsPerEmulator = 12
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

val isLocalCheck = project.providers.gradleProperty("localCheck").getOrElse("false").toBoolean()

if (!isLocalCheck) {
    tasks.check {
        dependsOn(tasks.named("instrumentationPRCheckKubernetes"))
    }
}
