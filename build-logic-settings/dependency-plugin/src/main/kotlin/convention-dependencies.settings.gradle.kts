import com.avito.android.artifactory.setUrlOrProxy

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val artifactoryUrl: String? by settings

dependencyResolutionManagement {

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }

    repositories {
        maven {
            setUrlOrProxy(
                artifactoryUrl = artifactoryUrl,
                repositoryName = "mavenCentral",
                originalRepo = "https://repo1.maven.org/maven2"
            )
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        repositoryName = "gradle-plugins",
                        originalRepo = "https://plugins.gradle.org/m2/"
                    )
                }
            }
            filter {
                includeModule("com.github.ben-manes", "gradle-versions-plugin")
                includeModule("org.gradle", "test-retry-gradle-plugin")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        repositoryName = "google-android",
                        originalRepo = "https://dl.google.com/dl/android/maven2/"
                    )
                }
            }
            filter {
                includeModuleByRegex("com\\.android.*", "(?!r8).*")
                includeModuleByRegex("com\\.google\\.android.*", ".*")
                includeGroupByRegex("androidx\\..*")
                includeGroup("com.google.testing.platform")
            }
        }
        // TODO: Remove this block after MBS-11267
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        repositoryName = "KotlinX",
                        originalRepo = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven"
                    )
                }
            }
            filter {
                includeModule("org.jetbrains.kotlinx", "kotlinx-html-jvm")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        repositoryName = "jitpack.io",
                        originalRepo = "https://jitpack.io"
                    )
                }
            }
            filter {
                includeModule("com.github.tiann", "FreeReflection")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        repositoryName = "jcenter",
                        originalRepo = "https://jcenter.bintray.com"
                    )
                }
            }
            filter {
                includeGroup("com.forkingcode.espresso.contrib")
                includeGroup("org.jetbrains.trove4j")
                includeModule("com.fkorotkov", "kubernetes-dsl")
                includeModule("org.jetbrains.teamcity", "teamcity-rest-client")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        repositoryName = "r8-releases",
                        originalRepo = "https://storage.googleapis.com/r8-releases/raw"
                    )
                }
            }
            filter {
                includeModule("com.android.tools", "r8")
            }
        }
    }
}
