rootProject.name = "android"

enableFeaturePreview("GRADLE_METADATA")

include(":utils")
include(":git")
include(":kotlin-dsl-support")
include(":okhttp")
include(":test-okhttp")
include(":test-project")
include(":android")
include(":time")

pluginManagement {

    repositories {
        jcenter()
        @Suppress("UnstableApiUsage")
        gradlePluginPortal()
        google()
    }

    val kotlinVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            }
        }
    }
}
