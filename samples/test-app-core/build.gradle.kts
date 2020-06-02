plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {

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

/**
 * delegateClosureOf used because kotlin dsl accessor
 * `fun Project.dependencies(configuration: DependencyHandlerScope.() -> Unit)`
 * is somehow unavailable for this and only this module.
 * It probably related to our custom plugins applied, but don't know how to debug this issue right now
 */
dependencies(delegateClosureOf<DependencyHandler> {
    api(Dependencies.test.junit)
    api(Dependencies.androidTest.rules)
    api("com.avito.android:test-inhouse-runner")

    androidTestRuntimeOnly(Dependencies.playServicesMaps) // todo remove, problem with test-inhouse-runner or ui-testing-core
})
