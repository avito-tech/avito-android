@file:Suppress("UnstableApiUsage")

rootProject.name = "avito-android-samples"

val useCompositeBuild: String by settings

include(":samples:test-app")
include(":samples:test-app-kaspresso")
if (useCompositeBuild.toBoolean()) {
    include(":samples:test-app-without-backward-compatibility")
}
include(":samples:test-app-core")
include(":samples:test-app-screenshot-test")

includeBuild("buildscript")

if (useCompositeBuild.toBoolean()) {
    includeBuild("subprojects") {
        dependencySubstitution {
            substitute(module("com.avito.android:instrumentation-tests")).with(project(":gradle:instrumentation-tests"))
            substitute(module("com.avito.android:proxy-toast")).with(project(":android-lib:proxy-toast"))
            substitute(module("com.avito.android:snackbar-proxy")).with(project(":android-lib:snackbar-proxy"))
            substitute(module("com.avito.android:snackbar-rule")).with(project(":android-test:snackbar-rule"))
            substitute(module("com.avito.android:time")).with(project(":common:time"))
            substitute(module("com.avito.android:test-report")).with(project(":android-test:test-report"))
            substitute(module("com.avito.android:test-screenshot")).with(project(":android-test:test-screenshot"))
            substitute(module("com.avito.android:junit-utils")).with(project(":common:junit-utils"))
            substitute(module("com.avito.android:toast-rule")).with(project(":android-test:toast-rule"))
            substitute(module("com.avito.android:test-inhouse-runner")).with(project(":android-test:test-inhouse-runner"))
            substitute(module("com.avito.android:test-annotations")).with(project(":common:test-annotations"))
            substitute(module("com.avito.android:ui-testing-core")).with(project(":android-test:ui-testing-core"))
            substitute(module("com.avito.android:report-viewer")).with(project(":common:report-viewer"))
            substitute(module("com.avito.android:file-storage")).with(project(":common:file-storage"))
            substitute(module("com.avito.android:okhttp")).with(project(":common:okhttp"))
        }
    }
}

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
                jcenter()
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
                gradlePluginPortal()
            }
            filter {
                includeGroup("com.gradle")
                includeGroup("org.jetbrains.kotlin.jvm")
                includeGroup("com.jfrog.bintray")
                includeGroup("com.slack.keeper")
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
    }

    val infraVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.android.") ->
                    useModule("com.android.tools.build:gradle:4.1.1")

                pluginId.startsWith("org.jetbrains.kotlin.") ->
                    useVersion("1.4.30")

                pluginId.startsWith("com.avito.android") ->
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:$infraVersion")

                pluginId == "com.slack.keeper" ->
                    useModule("com.slack.keeper:keeper:0.7.0")
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

dependencyResolutionManagement {
    repositories {
        maven {
            setUrlOrProxy("jcenter", "https://jcenter.bintray.com")
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
