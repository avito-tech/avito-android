@file:Suppress("UnstableApiUsage")

rootProject.name = "avito-android"

include(":subprojects:gradle:artifactory-app-backup")
include(":subprojects:gradle:buildchecks")
include(":subprojects:gradle:build-metrics")
include(":subprojects:gradle:build-properties")
include(":subprojects:gradle:cd")
include(":subprojects:gradle:dependencies-lint")
include(":subprojects:gradle:docs")
include(":subprojects:gradle:enforce-repos")
include(":subprojects:gradle:module-types")
include(":subprojects:gradle:bitbucket")
include(":subprojects:gradle:design-screenshots")
include(":subprojects:gradle:prosector")
include(":subprojects:gradle:logging")
include(":subprojects:gradle:robolectric")
include(":subprojects:gradle:room-config")
include(":subprojects:gradle:kotlin-root")
include(":subprojects:gradle:code-ownership")
include(":subprojects:gradle:performance")
include(":subprojects:gradle:pre-build")
include(":subprojects:gradle:kotlin-dsl-support")
include(":subprojects:gradle:kubernetes")
include(":subprojects:gradle:test-project")
include(":subprojects:gradle:files")
include(":subprojects:gradle:git")
include(":subprojects:gradle:impact-shared")
include(":subprojects:gradle:impact")
include(":subprojects:gradle:instrumentation-test-impact-analysis")
include(":subprojects:gradle:instrumentation-tests")
include(":subprojects:gradle:instrumentation-tests-default-config")
include(":subprojects:gradle:runner:client")
include(":subprojects:gradle:runner:service")
include(":subprojects:gradle:runner:shared")
include(":subprojects:gradle:runner:shared-test")
include(":subprojects:gradle:docker")
include(":subprojects:gradle:sentry-config")
include(":subprojects:gradle:statsd-config")
include(":subprojects:gradle:android")
include(":subprojects:gradle:lint-report")
include(":subprojects:gradle:feature-toggles")
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
include(":subprojects:common:logger")

include(":subprojects:android-test:resource-manager-exceptions")
include(":subprojects:android-test:websocket-reporter")
include(":subprojects:android-test:mockito-utils")
include(":subprojects:android-test:junit-utils")
include(":subprojects:android-test:test-annotations")
include(":subprojects:android-test:keep-for-testing")

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

    val artifactoryUrl: String? by settings
    val kotlinVersion: String by settings
    val androidGradlePluginVersion: String by settings
    val infraVersion: String by settings

    repositories {
        mavenLocal {
            content {
                includeGroupByRegex("com\\.avito\\.android\\.*")
            }
        }
        if (!artifactoryUrl.isNullOrBlank()) {
            maven {
                name = "Local Artifactory"
                setUrl("$artifactoryUrl/libs-release-local")
                content {
                    includeGroupByRegex("com\\.avito\\.android\\.*")
                }
            }
        }
        jcenter {
            content {
                includeGroupByRegex("com\\.avito\\.android\\.*")
            }
        }
        gradlePluginPortal()
        google()
    }

    plugins {
        id("digital.wup.android-maven-publish") version "3.6.3"
        id("kotlin") version kotlinVersion
        id("kotlin-android") version kotlinVersion
        id("nebula.integtest") version "7.0.7"
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
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:$infraVersion")

                pluginId == "com.slack.keeper" ->
                    useModule("com.slack.keeper:keeper:0.1.0")
            }
        }
    }
}
