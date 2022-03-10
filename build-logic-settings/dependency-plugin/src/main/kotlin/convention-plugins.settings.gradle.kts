import com.avito.android.artifactory.avitoRepoPrefix
import com.avito.android.artifactory.setUrlOrProxy

pluginManagement {

    val artifactoryUrl: String? by settings

    repositories {
        maven {
            setUrlOrProxy(
                artifactoryUrl = artifactoryUrl,
                repositoryName = "gradle-plugins",
                originalRepo = "https://plugins.gradle.org/m2/"
            )
        }
        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            forRepository {
                maven {
                    setUrlOrProxy(
                        artifactoryUrl = artifactoryUrl,
                        repositoryName = "mavenCentral",
                        originalRepo = "https://repo1.maven.org/maven2"
                    )
                }
            }
            filter {
                includeModuleByRegex("com\\.avito\\.android", ".*")
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
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("androidx.*")
                includeGroup("com.google.testing.platform")
            }
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
