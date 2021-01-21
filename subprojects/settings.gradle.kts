includeBuild("../libraries")
includeBuild("../build-logic")

include(":gradle:artifactory-app-backup")
include(":gradle:artifactory-app-backup-test-fixtures")
include(":gradle:build-checks")
include(":gradle:build-metrics")
include(":gradle:build-metrics-tracker")
include(":gradle:critical-path:critical-path")
include(":gradle:critical-path:api")
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
include(":test-runner:client")
include(":test-runner:device-provider")
include(":test-runner:report")
include(":test-runner:service")
include(":test-runner:command-line-executor")
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
include(":gradle:qapps")
include(":gradle:tms")
include(":gradle:trace-event")
include(":gradle:process")
include(":gradle:test-summary")
include(":gradle:slack")
include(":gradle:slack-test-fixtures")
include(":gradle:build-failer")
include(":gradle:build-failer-test-fixtures")
include(":gradle:build-environment")
include(":gradle:worker")
include(":gradle:module-dependencies-graph")

include(":common:build-metadata")
include(":common:resources")
include(":common:files")
include(":common:time")
include(":common:okhttp")
include(":common:test-okhttp")
include(":common:result")
include(":common:elastic")
include(":common:http-client")
include(":common:sentry")
include(":common:graphite")
include(":common:statsd")
include(":common:statsd-test-fixtures")
include(":common:problem")
include(":common:waiter")
include(":common:kotlin-ast-parser")
include(":common:random-utils")
include(":common:teamcity-common")
include(":common:junit-utils")
include(":common:graph")
include(":common:math")
include(":common:retrace")
include(":common:truth-extensions")
include(":common:composite-exception")
include(":common:throwable-utils")
include(":common:coroutines-extension")

include(":android-test:resource-manager-exceptions")
include(":android-test:websocket-reporter")
include(":android-test:keep-for-testing")
include(":android-test:ui-testing-maps")
include(":android-test:ui-testing-core-app")
include(":android-test:ui-testing-core")
include(":android-test:instrumentation")
include(":android-test:toast-rule")
include(":android-test:snackbar-rule")
include(":android-test:test-screenshot")
include(":android-test:rx3-idler")

include(":android-lib:proxy-toast")
include(":android-lib:snackbar-proxy")

include(":test-runner:test-report-artifacts")
include(":test-runner:test-annotations")
include(":test-runner:test-report-api")
include(":test-runner:test-report-dsl-api")
include(":test-runner:test-report-dsl")
include(":test-runner:test-report")
include(":test-runner:test-inhouse-runner")
include(":test-runner:test-instrumentation-runner")
include(":test-runner:k8s-deployments-cleaner")
include(":test-runner:instrumentation-changed-tests-finder")
include(":test-runner:instrumentation-tests")
include(":test-runner:instrumentation-tests-dex-loader")
include(":test-runner:file-storage")
include(":test-runner:report-viewer")
include(":test-runner:report-api")

include(":logger:gradle-logger")
include(":logger:android-log")
include(":logger:logger")
include(":logger:slf4j-logger")
include(":logger:sentry-logger")
include(":logger:elastic-logger")

include(":signer")

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
                includeGroup("com.github.ben-manes.versions")
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
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("KotlinX", "https://kotlin.bintray.com/kotlinx")
                }
            }
            filter {
                includeModule("org.jetbrains.kotlinx", "kotlinx-html-jvm")
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
                includeGroup("com.google.testing.platform")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("r8-releases", "https://storage.googleapis.com/r8-releases/raw")
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
val buildId = stringProperty("teamcityBuildId", nullIfBlank = true)

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        // Lost scans due to upload interruptions after build finishes
        isUploadInBackground = false
        publishAlwaysIf(isCI)
        if (buildId != null) value("buildId", buildId)
    }
}

fun booleanProperty(name: String, defaultValue: Boolean): Boolean {
    return if (settings.extra.has(name)) {
        settings.extra[name]?.toString()?.toBoolean() ?: defaultValue
    } else {
        defaultValue
    }
}

fun stringProperty(name: String, nullIfBlank: Boolean = false): String? {
    return if (settings.extra.has(name)) {
        val string = settings.extra[name]?.toString()
        if (nullIfBlank && string.isNullOrBlank()) null else string
    } else {
        null
    }
}
