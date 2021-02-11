plugins {
    id("com.avito.android.kotlin-android-library")
    id("com.avito.android.libraries")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
    implementation(libs.appcompat)

    androidTestImplementation(project(":subprojects:android-test:ui-testing-core"))

    androidTestUtil(libs.testOrchestrator)
}
