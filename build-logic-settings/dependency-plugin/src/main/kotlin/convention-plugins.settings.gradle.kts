import com.avito.android.artifactory.artifactoryUrl
import com.avito.android.artifactory.avitoRepoPrefix
import com.avito.android.artifactory.setUrlOrProxy

pluginManagement {

    val artifactoryUrl: String? by settings

    repositories {
        exclusiveContent {
            forRepositories(
                maven {
                    name = "Artifactory libs-release-local"
                    artifactoryUrl(
                        artifactoryUrl = artifactoryUrl,
                        artifactoryRepositoryName = "libs-release-local",
                    )
                },
                mavenLocal(),
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        artifactoryRepositoryName = "mavenCentral",
                        originalRepo = "https://repo1.maven.org/maven2"
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
                        artifactoryRepositoryName = "google-android",
                        originalRepo = "https://dl.google.com/dl/android/maven2/"
                    )
                }
            }
            filter {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("androidx.*")
                includeGroup("com.google.testing.platform")
            }
        }
        maven {
            setUrlOrProxy(
                artifactoryUrl = artifactoryUrl,
                artifactoryRepositoryName = "gradle-plugins",
                originalRepo = "https://plugins.gradle.org/m2/"
            )
        }

        /**
         * Removing implicitly added Gradle Plugins Repo, to prevent download avoiding artifactory
         */
        withType<MavenArtifactRepository> {
            if (!name.contains(avitoRepoPrefix) && name != "MavenLocal") {
                remove(this)
            }
        }
    }

    val infraVersion = providers.systemProperty("infraVersion")

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.avito.android") ->
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:${infraVersion.get()}")
            }
        }
    }
}
