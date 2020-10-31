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
        compileSdkVersion(28)
        testInstrumentationRunner = "com.avito.android.kaspressoui.test.KaspressoTestRunner"

        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "KaspressoAndroidTestApp",
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

    implementation(Dependencies.appcompat)

    androidTestImplementation("com.avito.android:test-inhouse-runner")

    androidTestImplementation(Dependencies.AndroidTest.runner)
    androidTestImplementation(Dependencies.AndroidTest.kaspresso)
    androidTestUtil(Dependencies.AndroidTest.orchestrator)

    androidTestImplementation(Dependencies.Test.junit)
})
