@file:Suppress("UnstableApiUsage")

rootProject.name = "avito-android-infra"

includeBuild("../buildscript")

include(":gradle:artifactory-app-backup")
include(":gradle:artifactory-app-backup-test-fixtures")
include(":gradle:buildchecks")
include(":gradle:build-metrics")
include(":gradle:gradle-profile")
include(":gradle:build-properties")
include(":gradle:build-trace")
include(":gradle:build-verdict")
include(":gradle:build-verdict-tasks-api")
include(":gradle:cd")
include(":gradle:module-types")
include(":gradle:bitbucket")
include(":gradle:design-screenshots")
include(":gradle:prosector")
include(":gradle:logging-test-fixtures")
include(":gradle:robolectric")
include(":gradle:room-config")
include(":gradle:code-ownership")
include(":gradle:pre-build")
include(":gradle:kotlin-dsl-support")
include(":gradle:kubernetes")
include(":gradle:test-project")
include(":gradle:git")
include(":gradle:git-test-fixtures")
include(":gradle:impact-shared")
include(":gradle:impact-shared-test-fixtures")
include(":gradle:impact")
include(":gradle:instrumentation-test-impact-analysis")
include(":gradle:instrumentation-changed-tests-finder")
include(":gradle:instrumentation-tests")
include(":gradle:instrumentation-tests-dex-loader")
include(":gradle:instrumentation-tests-test-fixtures")
include(":gradle:instrumentation-tests-dex-loader-test-fixtures")
include(":gradle:runner:client")
include(":gradle:runner:service")
include(":gradle:runner:shared")
include(":gradle:runner:shared-test")
include(":gradle:docker")
include(":gradle:sentry-config")
include(":gradle:graphite-config")
include(":gradle:statsd-config")
include(":gradle:android")
include(":gradle:lint-report")
include(":gradle:feature-toggles")
include(":gradle:ui-test-bytecode-analyzer")
include(":gradle:upload-cd-build-result")
include(":gradle:upload-to-googleplay")
include(":gradle:teamcity")
include(":gradle:signer")
include(":gradle:qapps")
include(":gradle:tms")
include(":gradle:trace-event")
include(":gradle:process")
include(":gradle:process-test-fixtures")
include(":gradle:test-summary")
include(":gradle:slack")
include(":gradle:slack-test-fixtures")
include(":gradle:build-failer")
include(":gradle:build-failer-test-fixtures")
include(":gradle:build-environment")
include(":gradle:worker")
include(":gradle:ci-logger")

include(":common:files")
include(":common:time")
include(":common:time-test-fixtures")
include(":common:okhttp")
include(":common:file-storage")
include(":common:test-okhttp")
include(":common:report-viewer")
include(":common:report-viewer-test-fixtures")
include(":common:elastic")
include(":common:sentry")
include(":common:graphite")
include(":common:graphite-test-fixtures")
include(":common:statsd")
include(":common:statsd-test-fixtures")
include(":common:logger")
include(":common:waiter")
include(":common:kotlin-ast-parser")
include(":common:random-utils")
include(":common:percent")
include(":common:logger-test-fixtures")
include(":common:teamcity-common")
include(":common:test-annotations")
include(":common:junit-utils")
include(":common:retrace")
include(":common:truth-extensions")
include(":common:composite-exception")
include(":common:throwable-utils")
include(":common:coroutines-extension")

include(":android-test:resource-manager-exceptions")
include(":android-test:websocket-reporter")
include(":android-test:keep-for-testing")
include(":android-test:ui-testing-maps")
include(":android-test:ui-testing-core")
include(":android-test:test-report")
include(":android-test:test-inhouse-runner")
include(":android-test:test-library")
include(":android-test:toast-rule")
include(":android-test:snackbar-rule")
include(":android-test:test-screenshot")
include(":android-lib:proxy-toast")
include(":android-lib:snackbar-proxy")
include(":android-test:test-instrumentation-runner")

include(":ci:k8s-deployments-cleaner")

pluginManagement {

    val kotlinVersion: String by System.getProperties()
    val androidGradlePluginVersion: String by System.getProperties()

    repositories {
        exclusiveContent {
            forRepository {
                gradlePluginPortal()
            }
            filter {
                includeGroup("com.gradle")
                includeGroup("com.gradle.enterprise")
                includeGroup("org.jetbrains.kotlin.jvm")
                includeGroup("com.jfrog.bintray")
                includeGroup("nebula.integtest")
                includeGroup("io.gitlab.arturbosch.detekt")
                includeGroup("com.autonomousapps.dependency-analysis")
            }
        }
        exclusiveContent {
            forRepository {
                google()
            }
            filter {
                includeGroupByRegex("com\\.android\\.tools\\.build\\.*")
            }
        }
    }

    plugins {
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

                pluginId == "com.autonomousapps.dependency-analysis" ->
                    useVersion("0.55.0")
            }
        }
    }
}

plugins {
    id("com.gradle.enterprise") version "3.3.4"
}
val isCI = booleanProperty("ci", false)

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailureIf(isCI)
    }
}

fun booleanProperty(name: String, defaultValue: Boolean): Boolean {
    return if (settings.extra.has(name)) {
        settings.extra[name]?.toString()?.toBoolean() ?: defaultValue
    } else {
        defaultValue
    }
}
