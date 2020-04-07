import com.android.build.gradle.ProguardFiles.ProguardFile
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator22
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator27
import com.avito.instrumentation.reservation.request.Device.LocalEmulator
import com.avito.kotlin.dsl.getOptionalStringProperty

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.avito.android.instrumentation-tests")
    id("com.slack.keeper")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "com.avito.android.ui.test.TestAppRunner"

        // TODO: protect from blank values
        // TODO: get rid of unnecessary values
        // TODO: describe in docs that they are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "AndroidTestApp",
                "unnecessaryUrl" to "https://localhost"
            )
        )
    }

    buildTypes {
        register("staging") {

            initWith(named("debug").get())

            setMatchingFallbacks("debug")

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile(ProguardFile.OPTIMIZE.fileName), "proguard-rules.pro")
        }
    }

    testBuildType = getOptionalStringProperty("testBuildType", "staging")

    /**
     * Disable all buildTypes except testing
     * to avoid confusing errors in IDE if wrong build variant is selected
     */
    variantFilter {
        if (name != testBuildType) {
            setIgnore(true)
            logger.debug("Build variant $name is omitted for module: $path")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

keeper {
    automaticR8RepoManagement.set(false)
}

/**
 * delegateClosureOf used because kotlin dsl accessor
 * `fun Project.dependencies(configuration: DependencyHandlerScope.() -> Unit)`
 * is somehow unavailable for this and only this module.
 * It probably related to our custom plugins applied, but don't know how to debug this issue right now
 */
dependencies(delegateClosureOf<DependencyHandler> {
    keeperR8(Dependencies.r8)

    implementation(project(":subprojects:android-lib:proxy-toast"))

    implementation(Dependencies.playServicesMaps)

    implementation(Dependencies.appcompat)
    implementation(Dependencies.recyclerView)
    implementation(Dependencies.material)

    androidTestImplementation(project(":subprojects:android-test:test-inhouse-runner"))
    androidTestImplementation(project(":subprojects:android-test:test-report"))
    androidTestImplementation(project(":subprojects:android-test:junit-utils"))
    androidTestImplementation(project(":subprojects:android-test:toast-rule"))
    androidTestImplementation(project(":subprojects:android-test:test-annotations"))
    androidTestImplementation(project(":subprojects:android-test:ui-testing-core"))
    androidTestImplementation(project(":subprojects:common:report-viewer"))
    androidTestImplementation(project(":subprojects:common:file-storage"))
    androidTestImplementation(project(":subprojects:common:okhttp"))
    androidTestImplementation(project(":subprojects:common:time"))

    androidTestImplementation(Dependencies.androidTest.runner)
    androidTestUtil(Dependencies.androidTest.orchestrator)

    androidTestImplementation(Dependencies.test.junit)
    androidTestImplementation(Dependencies.okhttp)
    androidTestImplementation(Dependencies.okhttpLogging)
    androidTestImplementation(Dependencies.funktionaleTry)
    androidTestImplementation(Dependencies.gson)
    androidTestImplementation(Dependencies.kotson)
    androidTestImplementation(Dependencies.sentry)
    androidTestImplementation(Dependencies.test.truth)
    androidTestImplementation(Dependencies.test.okhttpMockWebServer)
})

extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

    //todo make these params optional features in plugin
    reportApiUrl = project.getOptionalStringProperty("avito.report.url") ?: "http://stub"
    reportApiFallbackUrl = project.getOptionalStringProperty("avito.report.fallbackUrl") ?: "http://stub"
    reportViewerUrl = project.getOptionalStringProperty("avito.report.viewerUrl") ?: "http://stub"
    registry = project.getOptionalStringProperty("avito.registry") ?: "registry"
    sentryDsn =
        project.getOptionalStringProperty("avito.instrumentaion.sentry.dsn") ?: "http://stub-project@stub-host/0"
    slackToken = project.getOptionalStringProperty("avito.slack.token") ?: "stub"
    fileStorageUrl = project.getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"
    // deprecated since 2020.3.6
    output = project.rootProject.file("outputs/${project.name}/instrumentation").path

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
        "ito.android.de:*", //по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
        "*:E"
    )

    instrumentationParams = mapOf(
        "videoRecording" to "failed",
        "jobSlug" to "FunctionalTests"
    )

    configurationsContainer.register("Local") {
        tryToReRunOnTargetBranch = false
        reportSkippedTests = true
        rerunFailedTests = true
        reportFlakyTests = false

        targetsContainer.register("api27") {
            deviceName = "API27"

            scheduling = SchedulingConfiguration().apply {
                quota = QuotaConfiguration().apply {
                    retryCount = 1
                    minimumSuccessCount = 1
                }

                reservation = TestsBasedDevicesReservationConfiguration().apply {
                    device = LocalEmulator.device(27)
                    maximum = 1
                    minimum = 1
                    testsPerEmulator = 1
                }
            }
        }
    }

    configurationsContainer.register("ui") {
        tryToReRunOnTargetBranch = false
        reportSkippedTests = true
        rerunFailedTests = true
        reportFlakyTests = true

        targetsContainer.register("api22") {
            deviceName = "API22"

            scheduling = SchedulingConfiguration().apply {
                quota = QuotaConfiguration().apply {
                    retryCount = 1
                    minimumSuccessCount = 1
                }

                reservation = TestsBasedDevicesReservationConfiguration().apply {
                    device = Emulator22
                    maximum = 50
                    minimum = 2
                    testsPerEmulator = 3
                }
            }
        }

        targetsContainer.register("api27") {
            deviceName = "API27"

            scheduling = SchedulingConfiguration().apply {
                quota = QuotaConfiguration().apply {
                    retryCount = 1
                    minimumSuccessCount = 1
                }

                reservation = TestsBasedDevicesReservationConfiguration().apply {
                    device = Emulator27
                    maximum = 50
                    minimum = 2
                    testsPerEmulator = 3
                }
            }
        }
    }

    configurationsContainer.register("uiDebug") {
        tryToReRunOnTargetBranch = false
        reportSkippedTests = false
        rerunFailedTests = false
        reportFlakyTests = false
        // uncomment after 2020.4.1 release (MBS-8050)
        // enableDeviceDebug = true

        targetsContainer.register("api27") {
            deviceName = "API27"

            scheduling = SchedulingConfiguration().apply {
                quota = QuotaConfiguration().apply {
                    retryCount = 1
                    minimumSuccessCount = 1
                }

                reservation = TestsBasedDevicesReservationConfiguration().apply {
                    device = Emulator27
                    maximum = 1
                    minimum = 1
                    testsPerEmulator = 1
                }
            }
        }
    }
}

configurations.all {
    if (name.contains("AndroidTestRuntimeClasspath")) {
        resolutionStrategy {
            force("org.jetbrains:annotations:16.0.1")
        }
    }
}
