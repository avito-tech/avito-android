includeBuild("../libraries")
includeBuild("../build-logic")

includeBuild("../subprojects") {
    dependencySubstitution {
        substitute(module("com.avito.android:instrumentation-tests:local")).with(project(":gradle:instrumentation-tests"))
        substitute(module("com.avito.android:test-inhouse-runner:local")).with(project(":android-test:test-inhouse-runner"))
        substitute(module("com.avito.android:test-report:local")).with(project(":android-test:test-report"))
        substitute(module("com.avito.android:junit-utils:local")).with(project(":common:junit-utils"))
        substitute(module("com.avito.android:truth-extensions:local")).with(project(":common:truth-extensions"))
        substitute(module("com.avito.android:test-annotations:local")).with(project(":common:test-annotations"))
        substitute(module("com.avito.android:report-viewer:local")).with(project(":common:report-viewer"))
    }
}

include(":instrumentation")

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
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:local")

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
