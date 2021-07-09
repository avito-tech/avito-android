import com.avito.android.artifactory.setUrlOrProxy
import org.gradle.kotlin.dsl.provideDelegate

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
    }

    val infraVersion = providers.systemProperty("infraVersion").forUseAtConfigurationTime()

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
