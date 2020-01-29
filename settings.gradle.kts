@file:Suppress("UnstableApiUsage")

rootProject.name = "avito-android"

enableFeaturePreview("GRADLE_METADATA")

include(":upload-cd-build-result")
include(":bitbucket")
include(":lint-report")
include(":logging")
include(":utils")
include(":git")
include(":kubernetes")
include(":process")
include(":artifactory")
include(":kotlin-dsl-support")
include(":robolectric-config")
include(":room-config")
include(":kotlin-config")
include(":test-summary")
include(":file-storage")
include(":sentry")
include(":files")
include(":ui-test-bytecode-analyzer")
include(":instrumentation-impact-analysis")
include(":prosector")
include(":slack")
include(":ownership")
include(":performance")
include(":statsd")
include(":module-type")
include(":okhttp")
include(":report-viewer")
include(":docker")
include(":teamcity")
include(":instrumentation")
include(":docs-deployer")
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
include(":pre-build")
include(":ownership")
include(":time")
include(":runner:client")
include(":runner:service")
include(":runner:shared")
include(":runner:shared-test")
include(":enforce-repos")
include(":test-annotations")
include(":feature-toggle-report")
include(":websocket-reporter")
include(":resource-manager-exceptions")
include(":junit-utils")
include(":mockito-utils")

//Android modules
include(":ui-testing-maps")
include(":ui-testing-core")
include(":test-report")
include(":test-inhouse-runner")
include(":test-app")

pluginManagement {

    val artifactoryUrl: String by settings
    val kotlinVersion: String by settings
    val androidGradlePluginVersion: String by settings
    val infraVersion: String by settings

    repositories {
        maven {
            name = "Local Artifactory"
            setUrl("$artifactoryUrl/libs-release-local")
            content {
                includeGroupByRegex("com\\.avito\\.android\\..*")
            }
        }
        gradlePluginPortal()
        google()
        mavenLocal {
            content {
                includeGroupByRegex("com\\.avito\\.android\\..*")
            }
        }
    }

    plugins {
        id("digital.wup.android-maven-publish") version "3.6.3"
        id("kotlin") version kotlinVersion
        id("kotlin-android") version kotlinVersion
    }

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.android.") ->
                    useModule("com.android.tools.build:gradle:$androidGradlePluginVersion")

                pluginId.startsWith("org.jetbrains.kotlin.") ->
                    useVersion(kotlinVersion)

                pluginId.startsWith("com.avito.android") ->
                    useVersion(infraVersion)
            }
        }
    }
}
