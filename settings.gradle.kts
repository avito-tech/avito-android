rootProject.name = "avito-android"

enableFeaturePreview("GRADLE_METADATA")

include(":upload-cd-build-result")
include(":bitbucket")
include(":lint-report")
include(":logging")
include(":utils")
include(":git")
include(":kotlin-dsl-support")
include(":robolectric-config")
include(":room-config")
include(":kotlin-config")
include(":test-summary")
include(":file-storage")
include(":sentry")
include(":ui-test-bytecode-analyzer")
include(":instrumentation-impact-analysis")
include(":slack")
include(":statsd")
include(":okhttp")
include(":report-viewer")
include(":docker")
include(":teamcity")
include(":instrumentation")
include(":trace-event")
include(":test-okhttp")
include(":test-project")
include(":impact")
include(":impact-plugin")
include(":signer")
include(":build-metrics")
include(":build-checks")
include(":android")
include(":build-properties")
include(":qapps")
include(":time")
include(":runner:client")
include(":runner:service")
include(":runner:shared")
include(":runner:shared-test")
include(":enforce-repos")

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
