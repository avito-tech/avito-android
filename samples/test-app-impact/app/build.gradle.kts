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
    screenMarkerClass.set("AnyClass")
    screenMarkerMetadataField.set("anyField")
}

/**
 * delegateClosureOf used because kotlin dsl accessor
 * `fun Project.dependencies(configuration: DependencyHandlerScope.() -> Unit)`
 * is somehow unavailable for this and only this module.
 * It probably related to our custom plugins applied, but don't know how to debug this issue right now
 */
dependencies(delegateClosureOf<DependencyHandler> {
})
