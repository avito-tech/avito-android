import com.avito.android.artifactory.artifactoryUrl
import com.avito.android.artifactory.ensureUseOnlyProxies
import com.avito.android.artifactory.setUrlOrProxy
import com.avito.booleanProperty

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {

    val isInternalBuild = booleanProperty("avito.internalBuild", false)
    val artifactoryUrl: String? by settings

    @Suppress("UnstableApiUsage")
    repositories {
        maven {
            setUrlOrProxy(
                artifactoryUrl = artifactoryUrl,
                artifactoryRepositoryName = "mavenCentral",
                originalRepo = "https://repo1.maven.org/maven2"
            )
        }
        exclusiveContent {
            forRepositories(
                mavenLocal(),
                maven {
                    artifactoryUrl(
                        artifactoryUrl = artifactoryUrl,
                        artifactoryRepositoryName = "libs-release-local",
                    )
                }
            )

            filter {
                includeModuleByRegex("com\\.avito\\.android", ".*")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        artifactoryRepositoryName = "gradle-plugins",
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
                        artifactoryRepositoryName = "google-android",
                        originalRepo = "https://dl.google.com/dl/android/maven2/"
                    )
                }
            }
            filter {
                includeModuleByRegex("com\\.android.*", "(?!r8).*")
                includeModuleByRegex("com\\.google\\.android.*", "(?!annotations).*")
                includeGroupByRegex("androidx\\..*")
                includeGroup("com.google.testing.platform")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        artifactoryRepositoryName = "jitpack.io",
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
                        artifactoryRepositoryName = "jcenter",
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
                        artifactoryRepositoryName = "r8-releases",
                        originalRepo = "https://storage.googleapis.com/r8-releases/raw"
                    )
                }
            }
            filter {
                includeModule("com.android.tools", "r8")
            }
        }
        if (isInternalBuild) {
            ensureUseOnlyProxies(artifactoryUrl!!)
        }
    }
}
