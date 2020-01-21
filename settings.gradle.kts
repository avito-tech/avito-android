@file:Suppress("UnstableApiUsage")

rootProject.name = "avito-android"

enableFeaturePreview("GRADLE_METADATA")

include(":upload-cd-build-result")
include(":bitbucket")
include(":lint-report")
include(":logging")
include(":utils")
include(":git")
include(":artifactory")
include(":kotlin-dsl-support")
include(":robolectric-config")
include(":room-config")
include(":kotlin-config")
include(":test-summary")
include(":file-storage")
include(":sentry")
include(":ui-testing-core")
include(":ui-testing-maps")
include(":ui-test-bytecode-analyzer")
include(":instrumentation-impact-analysis")
include(":prosector")
include(":slack")
include(":ownership")
include(":performance")
include(":meta")
include(":statsd")
include(":module-type")
include(":okhttp")
include(":report-viewer")
include(":docker")
include(":teamcity")
include(":instrumentation")
include(":docs")
include(":trace-event")
include(":test-okhttp")
include(":test-project")
include(":upload-to-googleplay")
include(":impact")
include(":impact-plugin")
include(":dependencies-lint")
include(":signer")
include(":design-screenshots")
include(":build-metrics")
include(":build-checks")
include(":android")
include(":cicd")
include(":build-properties")
include(":qapps")
include(":time")
include(":runner:client")
include(":runner:service")
include(":runner:shared")
include(":runner:shared-test")
include(":enforce-repos")
include(":test-annotations")

pluginManagement {

    repositories {
        gradlePluginPortal()
        google()
    }

    val kotlinVersion: String by settings
    val androidGradlePluginVersion: String by settings

    plugins {
        id("digital.wup.android-maven-publish") version "3.6.3"
    }

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.android.") ->
                    useModule("com.android.tools.build:gradle:$androidGradlePluginVersion")

                pluginId.startsWith("org.jetbrains.kotlin.") ->
                    useVersion(kotlinVersion)
            }
        }
    }
}
