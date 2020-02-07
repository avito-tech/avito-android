import com.android.build.gradle.ProguardFiles.ProguardFile
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator22
import com.avito.instrumentation.reservation.request.Device.Emulator.Emulator27
import com.avito.kotlin.dsl.getOptionalStringProperty

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.avito.android.instrumentation-tests")
    id("com.slack.keeper")
}

val androidXVersion: String by project
val sentryVersion: String by project
val truthVersion: String by project
val okhttpVersion: String by project
val junitVersion: String by project
val funktionaleVersion: String by project
val gsonVersion: String by project
val kotsonVersion: String by project
val r8Version: String by project

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
                "slackToken" to "STUB",
                "sentryDsn" to "http://stub-project@stub-host/0",
                "fileStorageUrl" to "https://stub",
                "reportApiUrl" to "https://stub",
                "reportApiFallbackUrl" to "https://stub",
                "reportViewerUrl" to "https://stub",
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

    testBuildType = "staging"

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

dependencies {
    keeperR8("com.android.tools:r8:$r8Version")

    implementation("com.google.android.gms:play-services-maps:17.0.0")

    implementation("androidx.appcompat:appcompat:$androidXVersion")
    implementation("androidx.recyclerview:recyclerview:$androidXVersion")
    implementation("com.google.android.material:material:$androidXVersion")

    androidTestImplementation(project(":subprojects:android-test:test-inhouse-runner"))
    androidTestImplementation(project(":subprojects:android-test:test-report"))
    androidTestImplementation(project(":subprojects:android-test:junit-utils"))
    androidTestImplementation(project(":subprojects:android-test:test-annotations"))
    androidTestImplementation(project(":subprojects:android-test:ui-testing-core"))
    androidTestImplementation(project(":subprojects:common:report-viewer"))
    androidTestImplementation(project(":subprojects:common:file-storage"))
    androidTestImplementation(project(":subprojects:common:okhttp"))
    androidTestImplementation(project(":subprojects:common:time"))

    androidTestImplementation("junit:junit:$junitVersion")
    androidTestImplementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    androidTestImplementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    androidTestImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
    androidTestImplementation("com.google.code.gson:gson:$gsonVersion")
    androidTestImplementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")
    androidTestImplementation("io.sentry:sentry:$sentryVersion")
    androidTestImplementation("com.google.truth:truth:$truthVersion")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}

tasks.getByName("build").dependsOn("$path:instrumentationUi")

extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

    //todo make these params optional features in plugin
    reportApiUrl = project.getOptionalStringProperty("avito.report.url") ?: "http://stub"
    reportApiFallbackUrl = project.getOptionalStringProperty("avito.report.fallbackUrl") ?: "http://stub"
    reportViewerUrl = project.getOptionalStringProperty("avito.report.viewerUrl") ?: "http://stub"
    registry = project.getOptionalStringProperty("avito.registry") ?: "registry"
    sentryDsn = project.getOptionalStringProperty("avito.instrumentaion.sentry.dsn") ?: "stub"
    slackToken = project.getOptionalStringProperty("avito.slack.token") ?: "stub"
    fileStorageUrl = project.getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"

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
        "ito.android.de:*", //по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
        "*:E"
    )

    instrumentationParams = mapOf(
        "videoRecording" to "failed",
        "jobSlug" to "FunctionalTests"
    )

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
}

configurations.all {
    if (name.contains("AndroidTestRuntimeClasspath")) {
        resolutionStrategy {
            force("org.jetbrains:annotations:16.0.1")
        }
    }
}
