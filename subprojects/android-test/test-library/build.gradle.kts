plugins {
    id("com.android.library")
    id("kotlin-android")
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

dependencies(
    delegateClosureOf<DependencyHandler> {

        implementation(Dependencies.appcompat)

        androidTestImplementation(project(":subprojects:android-test:ui-testing-core"))
        androidTestUtil(Dependencies.AndroidTest.orchestrator)
    }
)
