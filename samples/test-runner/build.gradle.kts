plugins {
    id("convention.kotlin-android-app")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    packagingOptions {
        pickFirst("META-INF/okhttp.kotlin_module")
    }
}

dependencies {
    androidTestImplementation("com.avito.android:test-inhouse-runner")
    androidTestImplementation(libs.truth)

    androidTestUtil(libs.testOrchestrator)
}
