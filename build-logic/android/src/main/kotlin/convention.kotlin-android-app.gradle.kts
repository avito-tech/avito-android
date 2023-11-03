plugins {
    id("com.android.application")
    id("convention.kotlin-android-base")
    id("convention.android-base")
    id("convention.detekt")
    id("convention.dependency-locking-android-app")
}

android {

    testBuildType = "debug"

    /**
     * Disable all buildTypes except testing
     * to avoid confusing errors in IDE if wrong build variant is selected
     */
    variantFilter {
        if (name != testBuildType) {
            ignore = true
            logger.lifecycle("Build variant $name is omitted for module: $path")
        }
    }

    buildTypes {
        getByName(testBuildType) {
            // libraries only built in release variant, see convention.kotlin-android-library
            matchingFallbacks += listOf("release")
        }
    }
}
