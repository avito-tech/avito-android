includeBuild("../libraries")
includeBuild("../build-logic")

include(":gradle:artifactory-app-backup")
include(":gradle:artifactory-app-backup-test-fixtures")
include(":gradle:build-checks")
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
include(":gradle:robolectric")
include(":gradle:room-config")
include(":gradle:code-ownership")
include(":gradle:pre-build")
include(":gradle:gradle-extensions")
include(":gradle:kubernetes")
include(":gradle:test-project")
include(":gradle:git")
include(":gradle:git-test-fixtures")
include(":gradle:impact-shared")
include(":gradle:impact-shared-test-fixtures")
include(":gradle:impact")
include(":gradle:instrumentation-changed-tests-finder")
include(":gradle:instrumentation-tests")
include(":gradle:instrumentation-tests-dex-loader")
include(":gradle:instrumentation-tests-test-fixtures")
include(":gradle:instrumentation-tests-dex-loader-test-fixtures")
include(":test-runner:client")
include(":test-runner:device-provider")
include(":test-runner:report")
include(":gradle:runner:stub")
include(":test-runner:service")
include(":gradle:runner:shared")
include(":gradle:runner:shared-test")
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
include(":gradle:gradle-logger")
include(":gradle:module-dependencies-graph")

include(":common:build-metadata")
include(":common:resources")
include(":common:files")
include(":common:time")
include(":common:okhttp")
include(":common:file-storage")
include(":common:test-okhttp")
include(":common:report-api")
include(":common:report-viewer")
include(":common:result")
include(":common:elastic-logger")
include(":common:elastic")
include(":common:http-client")
include(":common:sentry-logger")
include(":common:sentry")
include(":common:slf4j-logger")
include(":common:graphite")
include(":common:statsd")
include(":common:statsd-test-fixtures")
include(":common:test-report-artifacts")
include(":common:logger")
include(":common:waiter")
include(":common:kotlin-ast-parser")
include(":common:random-utils")
include(":common:teamcity-common")
include(":common:test-annotations")
include(":common:junit-utils")
include(":common:graph")
include(":common:math")
include(":common:retrace")
include(":common:truth-extensions")
include(":common:composite-exception")
include(":common:throwable-utils")
include(":common:coroutines-extension")
include(":common:test-report-api")
include(":common:test-report-dsl-api")
include(":common:test-report-dsl")

include(":android-test:resource-manager-exceptions")
include(":android-test:websocket-reporter")
include(":android-test:keep-for-testing")
include(":android-test:ui-testing-maps")
include(":android-test:ui-testing-core-app")
include(":android-test:ui-testing-core")
include(":android-test:test-report")
include(":android-test:test-inhouse-runner")
include(":android-test:instrumentation")
include(":android-test:test-library")
include(":android-test:toast-rule")
include(":android-test:snackbar-rule")
include(":android-test:test-screenshot")
include(":android-test:test-instrumentation-runner")
include(":android-test:android-log")
include(":android-test:rx3-idler")

include(":android-lib:proxy-toast")
include(":android-lib:snackbar-proxy")

include(":test-runner:k8s-deployments-cleaner")

@Suppress("UnstableApiUsage")
pluginManagement {

    val artifactoryUrl: String? by settings

    fun MavenArtifactRepository.artifactoryUrl(repositoryName: String) {
        setUrl("$artifactoryUrl/$repositoryName")
        isAllowInsecureProtocol = true
    }

    fun MavenArtifactRepository.setUrlOrProxy(repositoryName: String, originalRepo: String) {
        if (artifactoryUrl.isNullOrBlank()) {
            name = repositoryName
            setUrl(originalRepo)
        } else {
            name = "Proxy for $repositoryName: $originalRepo"
            artifactoryUrl(repositoryName)
        }
    }

    repositories {
        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            forRepository {
                maven {
                    setUrlOrProxy("mavenCentral", "https://repo1.maven.org/maven2")
                }
            }
            filter {
                includeModuleByRegex("com\\.avito\\.android", ".*")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("google-android", "https://dl.google.com/dl/android/maven2/")
                }
            }
            filter {
                includeGroupByRegex("com\\.android\\.tools\\.build\\.*")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("gradle-plugins", "https://plugins.gradle.org/m2/")
                }
            }
            filter {
                includeGroup("com.slack.keeper")
                includeGroup("com.gradle")
                includeGroup("com.gradle.enterprise")
                includeGroup("org.jetbrains.kotlin.jvm")
                includeGroup("org.gradle.kotlin.kotlin-dsl")
                includeGroupByRegex("nebula\\..*")
                includeGroup("io.gitlab.arturbosch.detekt")
                includeGroup("com.autonomousapps.dependency-analysis")
            }
        }
    }

    @Suppress("UnstableApiUsage")
    fun systemProperty(name: String): Provider<String> {
        return providers.systemProperty(name).forUseAtConfigurationTime()
    }

    val kotlinVersion = systemProperty("kotlinVersion")
    val detektVersion = systemProperty("detektVersion")
    val androidGradlePluginVersion = systemProperty("androidGradlePluginVersion")
    val infraVersion = systemProperty("infraVersion")
    val nebulaIntegTestVersion = systemProperty("nebulaIntegTestVersion")

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.android.") ->
                    useModule("com.android.tools.build:gradle:${androidGradlePluginVersion.get()}")

                pluginId.startsWith("org.jetbrains.kotlin.") ->
                    useVersion(kotlinVersion.get())

                pluginId.startsWith("com.avito.android") ->
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:${infraVersion.get()}")

                pluginId == "com.slack.keeper" ->
                    useModule("com.slack.keeper:keeper:0.7.0")

                pluginId == "nebula.integtest" ->
                    useVersion(nebulaIntegTestVersion.get())

                pluginId == "io.gitlab.arturbosch.detekt" ->
                    useVersion(detektVersion.get())
            }
        }
    }
}

val artifactoryUrl: String? by settings

fun MavenArtifactRepository.artifactoryUrl(repositoryName: String) {
    setUrl("$artifactoryUrl/$repositoryName")
    isAllowInsecureProtocol = true
}

fun MavenArtifactRepository.setUrlOrProxy(repositoryName: String, originalRepo: String) {
    if (artifactoryUrl.isNullOrBlank()) {
        name = repositoryName
        setUrl(originalRepo)
    } else {
        name = "Proxy for $repositoryName: $originalRepo"
        artifactoryUrl(repositoryName)
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        maven {
            setUrlOrProxy("mavenCentral", "https://repo1.maven.org/maven2")
        }

        // not available in mavenCentral
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("jcenter", "https://jcenter.bintray.com")
                }
            }
            filter {
                includeGroup("org.jetbrains.trove4j")
                includeGroup("com.forkingcode.espresso.contrib")
                includeModule("org.jetbrains.teamcity", "teamcity-rest-client")
                includeModule("com.fkorotkov", "kubernetes-dsl")
                includeModule("me.weishu", "free_reflection")
            }
        }

        // for kotlinx-cli https://github.com/Kotlin/kotlinx-cli/issues/23
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("KotlinX", "https://kotlin.bintray.com/kotlinx")
                }
            }
            filter {
                includeGroup("org.jetbrains.kotlinx")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("google-android", "https://dl.google.com/dl/android/maven2/")
                }
            }
            filter {
                includeModuleByRegex("com\\.android.*", "(?!r8).*")
                includeModuleByRegex("com\\.google\\.android.*", ".*")
                includeGroupByRegex("androidx\\..*")
                includeGroup("com.google.test.platform")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("r8-releases", "http://storage.googleapis.com/r8-releases/raw")
                }
            }
            filter {
                includeModule("com.android.tools", "r8")
            }
        }
    }
}

plugins {
    id("com.gradle.enterprise") version "3.6.1"
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
