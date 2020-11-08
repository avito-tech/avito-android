import com.avito.instrumentation.impact.InstrumentationTestImpactAnalysisExtension

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.avito.android.instrumentation-test-impact-analysis")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
    }

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

extensions.configure<InstrumentationTestImpactAnalysisExtension> {
    screenMarkerClass.set("com.avito.android.sample.impact.ImpactScreenMarker")
    screenMarkerMetadataField.set("id")
}

dependencies {
    implementation(project(":samples:test-app-impact:feature-one"))
    implementation(project(":samples:test-app-impact:feature-two"))
    implementation(Dependencies.appcompat)
    androidTestImplementation(Dependencies.Test.junit)
    androidTestImplementation(project(":samples:test-app-impact:androidTest-core"))
}
