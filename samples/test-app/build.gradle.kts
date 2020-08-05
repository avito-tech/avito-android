import com.android.build.gradle.ProguardFiles.ProguardFile
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
        testInstrumentationRunner = "com.avito.android.test.app.core.TestAppRunner"

        // TODO: describe in docs that they are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "AndroidTestApp"
            )
        )
    }

    buildTypes {

        register("staging") {

            initWith(named("debug").get())

            setMatchingFallbacks("debug")

            isMinifyEnabled = true
            isShrinkResources = false // https://github.com/slackhq/keeper/issues/22

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
            ignore = true
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

    implementation("com.avito.android:proxy-toast")

    implementation(Dependencies.playServicesMaps)

    implementation(Dependencies.appcompat)
    implementation(Dependencies.recyclerView)
    implementation(Dependencies.material)

    androidTestImplementation(project(":samples:test-app-core"))
    androidTestImplementation("com.avito.android:test-report")
    androidTestImplementation("com.avito.android:junit-utils")
    androidTestImplementation("com.avito.android:toast-rule")
    androidTestImplementation("com.avito.android:test-annotations")
    androidTestImplementation("com.avito.android:ui-testing-core")
    androidTestImplementation("com.avito.android:report-viewer")
    androidTestImplementation("com.avito.android:file-storage")
    androidTestImplementation("com.avito.android:okhttp")
    androidTestImplementation("com.avito.android:time")

    androidTestImplementation(Dependencies.androidTest.runner)
    androidTestUtil(Dependencies.androidTest.orchestrator)

    androidTestImplementation(Dependencies.okhttp)
    androidTestImplementation(Dependencies.okhttpLogging)
    androidTestImplementation(Dependencies.funktionaleTry)
    androidTestImplementation(Dependencies.gson)
    androidTestImplementation(Dependencies.kotson)
    androidTestImplementation(Dependencies.sentry)
    androidTestImplementation(Dependencies.test.mockitoCore)
    androidTestImplementation(Dependencies.test.truth)
    androidTestImplementation(Dependencies.test.okhttpMockWebServer)
})
