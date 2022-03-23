import com.avito.android.artifactory.setUrlOrProxy

pluginManagement {

    val artifactoryUrl: String? by settings

    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()

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

    val infraVersion = providers.systemProperty("infraVersion")

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.avito.android") -> {
                    println("=== Replace plugin $pluginId")
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:${infraVersion.get()}")
                }
            }
        }
    }
}
