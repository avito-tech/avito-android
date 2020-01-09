rootProject.name = "android"

enableFeaturePreview("GRADLE_METADATA")

include(":bitbucket")
include(":lint-report")
include(":logging")
include(":utils")
include(":git")
include(":kotlin-dsl-support")
include(":sentry")
include(":statsd")
include(":okhttp")
include(":docker")
include(":trace-event")
include(":test-okhttp")
include(":test-project")
include(":impact")
include(":android")
include(":build-properties")
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
