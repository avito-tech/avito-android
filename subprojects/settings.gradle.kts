@file:Suppress("UnstableApiUsage")

rootProject.name = "avito-android"

includeBuild("../buildscript")

include(":gradle:artifactory-app-backup")
include(":gradle:artifactory-app-backup-test-fixtures")
include(":gradle:buildchecks")
include(":gradle:build-metrics")
include(":gradle:build-properties")
include(":gradle:cd")
include(":gradle:dependencies-lint")
include(":gradle:module-types")
include(":gradle:bitbucket")
include(":gradle:design-screenshots")
include(":gradle:prosector")
include(":gradle:logging")
include(":gradle:logging-test-fixtures")
include(":gradle:robolectric")
include(":gradle:room-config")
include(":gradle:kotlin-root")
include(":gradle:code-ownership")
include(":gradle:performance")
include(":gradle:pre-build")
include(":gradle:kotlin-dsl-support")
include(":gradle:kubernetes")
include(":gradle:test-project")
include(":gradle:files")
include(":gradle:git")
include(":gradle:git-test-fixtures")
include(":gradle:impact-shared")
include(":gradle:impact")
include(":gradle:instrumentation-test-impact-analysis")
include(":gradle:instrumentation-tests")
include(":gradle:instrumentation-tests-test-fixtures")
include(":gradle:instrumentation-tests-default-config")
include(":gradle:runner:client")
include(":gradle:runner:service")
include(":gradle:runner:shared")
include(":gradle:runner:shared-test")
include(":gradle:docker")
include(":gradle:sentry-config")
include(":gradle:graphite-config")
include(":gradle:statsd-config")
include(":gradle:android")
include(":gradle:build-on-target")
include(":gradle:lint-report")
include(":gradle:feature-toggles")
include(":gradle:ui-test-bytecode-analyzer")
include(":gradle:upload-cd-build-result")
include(":gradle:upload-to-googleplay")
include(":gradle:teamcity")
include(":gradle:signer")
include(":gradle:qapps")
include(":gradle:trace-event")
include(":gradle:process")
include(":gradle:process-test-fixtures")
include(":gradle:test-summary")
include(":gradle:slack")
include(":gradle:slack-test-fixtures")
include(":gradle:utils")
include(":gradle:utils-test-fixtures")
include(":gradle:build-environment")

include(":common:time")
include(":common:time-test-fixtures")
include(":common:okhttp")
include(":common:file-storage")
include(":common:test-okhttp")
include(":common:report-viewer")
include(":common:report-viewer-test-fixtures")
include(":common:sentry")
include(":common:graphite")
include(":common:graphite-test-fixtures")
include(":common:statsd")
include(":common:statsd-test-fixtures")
include(":common:logger")
include(":common:teamcity-common")

include(":android-test:resource-manager-exceptions")
include(":android-test:websocket-reporter")
include(":android-test:junit-utils")
include(":android-test:test-annotations")
include(":android-test:keep-for-testing")
include(":android-test:ui-testing-maps")
include(":android-test:ui-testing-core")
include(":android-test:test-report")
include(":android-test:test-inhouse-runner")
include(":android-test:test-library")
include(":android-test:toast-rule")
include(":android-lib:proxy-toast")

include(":ci:k8s-deployments-cleaner")

pluginManagement {

    val artifactoryUrl: String? by settings
    val infraVersion: String by settings
    val kotlinVersion: String by System.getProperties()
    val androidGradlePluginVersion: String by System.getProperties()

    repositories {
        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            forRepository {
                jcenter()
            }
            forRepository {
                maven {
                    name = "Avito bintray"
                    setUrl("https://dl.bintray.com/avito/maven")
                }
            }
            if (!artifactoryUrl.isNullOrBlank()) {
                forRepository {
                    maven {
                        name = "Local Artifactory"
                        setUrl("$artifactoryUrl/libs-release-local")
                    }
                }
            }
            filter {
                includeModuleByRegex("com\\.avito\\.android", ".*")
            }
        }
        exclusiveContent {
            forRepository {
                gradlePluginPortal()
            }
            filter {
                includeGroup("org.jetbrains.kotlin.jvm")
                includeGroup("com.jfrog.bintray")
                includeGroup("com.slack.keeper")
                includeGroup("digital.wup.android-maven-publish")
                includeGroup("nebula.integtest")
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
        exclusiveContent {
            forRepository {
                maven {
                    name = "R8 releases"
                    setUrl("http://storage.googleapis.com/r8-releases/raw")
                }
            }
            filter {
                includeModule("com.android.tools", "r8")
            }
        }
    }

    plugins {
        id("digital.wup.android-maven-publish") version "3.6.3"
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
                    useModule("com.slack.keeper:keeper:0.2.0")
            }
        }
    }
}
