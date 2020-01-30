@file:Suppress("UnstableApiUsage")

rootProject.name = "avito-android"

include(":subprojects:gradle:artifactory")
include(":subprojects:gradle:build-checks")
include(":subprojects:gradle:build-metrics")
include(":subprojects:gradle:build-properties")
include(":subprojects:gradle:cicd")
include(":subprojects:gradle:dependencies-lint")
include(":subprojects:gradle:docs-deployer")
include(":subprojects:gradle:enforce-repos")
include(":subprojects:gradle:module-type")
include(":subprojects:gradle:bitbucket")
include(":subprojects:gradle:design-screenshots")
include(":subprojects:gradle:prosector")
include(":subprojects:gradle:logging")
include(":subprojects:gradle:robolectric-config")
include(":subprojects:gradle:room-config")
include(":subprojects:gradle:kotlin-config")
include(":subprojects:gradle:ownership")
include(":subprojects:gradle:performance")
include(":subprojects:gradle:pre-build")
include(":subprojects:gradle:kotlin-dsl-support")
include(":subprojects:gradle:kubernetes")
include(":subprojects:gradle:test-project")
include(":subprojects:gradle:files")
include(":subprojects:gradle:git")
include(":subprojects:gradle:impact")
include(":subprojects:gradle:impact-plugin")
include(":subprojects:gradle:instrumentation-impact-analysis")
include(":subprojects:gradle:instrumentation")
include(":subprojects:gradle:runner:client")
include(":subprojects:gradle:runner:service")
include(":subprojects:gradle:runner:shared")
include(":subprojects:gradle:runner:shared-test")
include(":subprojects:gradle:docker")
include(":subprojects:gradle:android")
include(":subprojects:gradle:lint-report")
include(":subprojects:gradle:feature-toggle-report")
include(":subprojects:gradle:ui-test-bytecode-analyzer")
include(":subprojects:gradle:upload-cd-build-result")
include(":subprojects:gradle:upload-to-googleplay")
include(":subprojects:gradle:teamcity")
include(":subprojects:gradle:signer")
include(":subprojects:gradle:qapps")
include(":subprojects:gradle:trace-event")
include(":subprojects:gradle:process")
include(":subprojects:gradle:test-summary")
include(":subprojects:gradle:slack")
include(":subprojects:gradle:utils")

include(":subprojects:common:time")
include(":subprojects:common:okhttp")
include(":subprojects:common:file-storage")
include(":subprojects:common:test-okhttp")
include(":subprojects:common:report-viewer")
include(":subprojects:common:sentry")
include(":subprojects:common:statsd")

include(":subprojects:android-test:resource-manager-exceptions")
include(":subprojects:android-test:websocket-reporter")
include(":subprojects:android-test:mockito-utils")
include(":subprojects:android-test:junit-utils")
include(":subprojects:android-test:test-annotations")

// see gradle.properties flag explanation
val syncAndroidModules: String by settings
if (syncAndroidModules.toBoolean()) {
    include(":subprojects:android-test:ui-testing-maps")
    include(":subprojects:android-test:ui-testing-core")
    include(":subprojects:android-test:test-report")
    include(":subprojects:android-test:test-inhouse-runner")
    include(":subprojects:android-test:test-app")
}

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
