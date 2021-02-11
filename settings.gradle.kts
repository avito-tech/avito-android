rootProject.name = "avito-android-infra"

include(":subprojects:gradle:artifactory-app-backup")
include(":subprojects:gradle:artifactory-app-backup-test-fixtures")
include(":subprojects:gradle:buildchecks")
include(":subprojects:gradle:build-metrics")
include(":subprojects:gradle:gradle-profile")
include(":subprojects:gradle:build-properties")
include(":subprojects:gradle:build-trace")
include(":subprojects:gradle:build-verdict")
include(":subprojects:gradle:build-verdict-tasks-api")
include(":subprojects:gradle:cd")
include(":subprojects:gradle:module-types")
include(":subprojects:gradle:bitbucket")
include(":subprojects:gradle:design-screenshots")
include(":subprojects:gradle:prosector")
include(":subprojects:gradle:robolectric")
include(":subprojects:gradle:room-config")
include(":subprojects:gradle:code-ownership")
include(":subprojects:gradle:pre-build")
include(":subprojects:gradle:gradle-extensions")
include(":subprojects:gradle:kubernetes")
include(":subprojects:gradle:test-project")
include(":subprojects:gradle:git")
include(":subprojects:gradle:git-test-fixtures")
include(":subprojects:gradle:impact-shared")
include(":subprojects:gradle:impact-shared-test-fixtures")
include(":subprojects:gradle:impact")
include(":subprojects:gradle:instrumentation-changed-tests-finder")
include(":subprojects:gradle:instrumentation-tests")
include(":subprojects:gradle:instrumentation-tests-dex-loader")
include(":subprojects:gradle:instrumentation-tests-test-fixtures")
include(":subprojects:gradle:instrumentation-tests-dex-loader-test-fixtures")
include(":subprojects:gradle:runner:client")
include(":subprojects:gradle:runner:device-provider")
include(":subprojects:gradle:runner:stub")
include(":subprojects:gradle:runner:service")
include(":subprojects:gradle:runner:shared")
include(":subprojects:gradle:runner:shared-test")
include(":subprojects:gradle:docker")
include(":subprojects:gradle:sentry-config")
include(":subprojects:gradle:graphite-config")
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
include(":subprojects:gradle:tms")
include(":subprojects:gradle:trace-event")
include(":subprojects:gradle:process")
include(":subprojects:gradle:process-test-fixtures")
include(":subprojects:gradle:test-summary")
include(":subprojects:gradle:slack")
include(":subprojects:gradle:slack-test-fixtures")
include(":subprojects:gradle:build-failer")
include(":subprojects:gradle:build-failer-test-fixtures")
include(":subprojects:gradle:build-environment")
include(":subprojects:gradle:worker")
include(":subprojects:gradle:gradle-logger")

include(":subprojects:common:resources")
include(":subprojects:common:files")
include(":subprojects:common:time")
include(":subprojects:common:okhttp")
include(":subprojects:common:file-storage")
include(":subprojects:common:test-okhttp")
include(":subprojects:common:report-viewer")
include(":subprojects:common:report-viewer-test-fixtures")
include(":subprojects:common:elastic-logger")
include(":subprojects:common:elastic")
include(":subprojects:common:sentry-logger")
include(":subprojects:common:sentry")
include(":subprojects:common:slf4j-logger")
include(":subprojects:common:graphite")
include(":subprojects:common:graphite-test-fixtures")
include(":subprojects:common:statsd")
include(":subprojects:common:statsd-test-fixtures")
include(":subprojects:common:logger")
include(":subprojects:common:waiter")
include(":subprojects:common:kotlin-ast-parser")
include(":subprojects:common:random-utils")
include(":subprojects:common:logger-test-fixtures")
include(":subprojects:common:teamcity-common")
include(":subprojects:common:test-annotations")
include(":subprojects:common:junit-utils")
include(":subprojects:common:math")
include(":subprojects:common:retrace")
include(":subprojects:common:truth-extensions")
include(":subprojects:common:composite-exception")
include(":subprojects:common:throwable-utils")
include(":subprojects:common:coroutines-extension")

include(":subprojects:android-test:resource-manager-exceptions")
include(":subprojects:android-test:websocket-reporter")
include(":subprojects:android-test:keep-for-testing")
include(":subprojects:android-test:ui-testing-maps")
include(":subprojects:android-test:ui-testing-core-app")
include(":subprojects:android-test:ui-testing-core")
include(":subprojects:android-test:test-report")
include(":subprojects:android-test:test-inhouse-runner")
include(":subprojects:android-test:test-library")
include(":subprojects:android-test:toast-rule")
include(":subprojects:android-test:snackbar-rule")
include(":subprojects:android-test:test-screenshot")
include(":subprojects:android-test:test-instrumentation-runner")
include(":subprojects:android-test:android-log")
include(":subprojects:android-test:rx3-idler")

include(":subprojects:android-lib:proxy-toast")
include(":subprojects:android-lib:snackbar-proxy")

include(":subprojects:ci:k8s-deployments-cleaner")

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
                mavenLocal()
            }
            if (!artifactoryUrl.isNullOrBlank()) {
                forRepository {
                    maven {
                        name = "Local Artifactory"
                        setUrl("$artifactoryUrl/libs-release-local")
                    }
                }
            }
            forRepository {
                maven {
                    setUrlOrProxy("jcenter", "https://jcenter.bintray.com")
                }
            }
            forRepository {
                maven {
                    setUrlOrProxy("bintray-avito-maven", "https://dl.bintray.com/avito/maven")
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
                    setUrlOrProxy("r8-releases", "http://storage.googleapis.com/r8-releases/raw")
                }
            }
            filter {
                includeModule("com.android.tools", "r8")
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
                includeGroup("com.jfrog.bintray")
                includeGroup("io.gitlab.arturbosch.detekt")
                includeGroup("com.autonomousapps.dependency-analysis")
            }
        }
    }

    val kotlinVersion = providers.systemProperty("kotlinVersion").forUseAtConfigurationTime()
    val detektVersion = providers.systemProperty("detektVersion").forUseAtConfigurationTime()
    val androidGradlePluginVersion = providers.systemProperty("androidGradlePluginVersion").forUseAtConfigurationTime()
    val infraVersion = providers.gradleProperty("infraVersion").forUseAtConfigurationTime()

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

                pluginId == "com.autonomousapps.dependency-analysis" ->
                    useVersion("0.55.0")

                pluginId == "nebula.integtest" ->
                    useVersion("8.0.0")

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
            setUrlOrProxy("jcenter", "https://jcenter.bintray.com")
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy("bintray-avito-maven", "https://dl.bintray.com/avito/maven")
                }
            }
            filter {
                includeModuleByRegex("com\\.avito\\.android", ".*")
            }
        }
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
                includeGroup("com.google.testing.platform")
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
    id("com.gradle.enterprise") version "3.5.1"
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
