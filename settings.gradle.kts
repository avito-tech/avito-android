rootProject.name = "android"

include(":utils")
include(":kotlin-dsl-support")
include(":test-project")
include(":android")

val kotlinVersion: String by settings

pluginManagement {

    repositories {
        jcenter()
        gradlePluginPortal()
        google()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            }
        }
    }
}
