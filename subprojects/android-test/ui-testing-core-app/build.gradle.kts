import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.KubernetesCredentials.Service
import com.avito.utils.gradle.kubernetesCredentials

plugins {
    id("convention.kotlin-android-app")
    id("com.avito.android.instrumentation-tests")
    id("com.avito.android.libraries")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "com.avito.android.test.app.core.TestAppRunner"

        // TODO: describe in docs that they are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "AndroidTestApp"
            )
        )
    }

    testBuildType = "debug"

    /**
     * Disable all buildTypes except testing
     * to avoid confusing errors in IDE if wrong build variant is selected
     */
    variantFilter {
        if (name != testBuildType) {
            ignore = true
            logger.debug("Build variant $name is omitted for module: $path")
        }
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

    implementation(project(":subprojects:android-lib:proxy-toast"))

    androidTestImplementation(project(":subprojects:android-test:toast-rule"))
    androidTestImplementation(project(":subprojects:android-test:ui-testing-core"))
    androidTestImplementation(project(":subprojects:android-test:test-inhouse-runner"))
    androidTestImplementation(project(":subprojects:common:junit-utils"))
    androidTestImplementation(project(":subprojects:common:truth-extensions"))
    androidTestImplementation(project(":subprojects:common:test-annotations"))
    androidTestImplementation(project(":subprojects:common:report-viewer"))

    androidTestUtil(libs.testOrchestrator)
}

instrumentation {
    reportApiUrl = getOptionalStringProperty("avito.report.url") ?: "http://stub"
    reportApiFallbackUrl = "http://stub" // todo remove
    reportViewerUrl = getOptionalStringProperty("avito.report.viewerUrl") ?: "http://stub"

    // todo pass whole image with registry instead of this MBS-10592
    registry = getOptionalStringProperty("avito.registry") ?: "stub"
    sentryDsn = getOptionalStringProperty("avito.instrumentaion.sentry.dsn") ?: "http://stub-project@stub-host/0"
    slackToken = getOptionalStringProperty("avito.slack.test.token") ?: "stub"
    fileStorageUrl = getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"

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
            image = "android/emulator-22:740eb9a948",
            cpuCoresRequest = "1",
            cpuCoresLimit = "1.3",
            memoryLimit = "4Gi"
        )

        val emulator29 = CloudEmulator(
            name = "api29",
            api = 29,
            model = "Android_SDK_built_for_x86_64",
            image = "android/emulator-29:915c1f20be",
            cpuCoresRequest = "1",
            cpuCoresLimit = "1.3",
            memoryLimit = "4Gi"
        )

        configurationsContainer.register("ui") {
            reportSkippedTests = true
            filter = "ci"

            targetsContainer.register("api22") {
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

            targetsContainer.register("api29") {
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
