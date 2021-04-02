plugins {
    id("convention.kotlin-android-library")
    id("convention.libraries")
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

    androidTestImplementation(projects.androidTest.uiTestingCore)

    androidTestUtil(libs.testOrchestrator)
}
