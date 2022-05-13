import org.gradle.api.artifacts.repositories.MavenArtifactRepository

// ==================================================================================================
// We apply this script inside `pluginManagement` block.
// It's done to configure `pluginManagement.repositories` _before_ applying the first plugin from included builds.
// `pluginManagement.repositories` are used to resolve dependencies of plugins from included builds.
// Using convention plugins to configure repositories is a chicken and egg problem.
// The first applied plugin would require already configured `pluginManagement.repositories` itself.
// `pluginManagement` and `plugins` blocks are evaluated before anything in a buildscript.
// ==================================================================================================

pluginManagement {

    fun Settings.booleanProperty(name: String, defaultValue: Boolean): Boolean {
        return if (extra.has(name)) {
            extra[name]?.toString()?.toBoolean() ?: defaultValue
        } else {
            defaultValue
        }
    }

    val isInternalBuild = booleanProperty("avito.internalBuild", false)
    val artifactoryUrl: String? by settings

    fun artifactoryUrl(): String {
        if (isInternalBuild) {
            require(!artifactoryUrl.isNullOrBlank()) {
                "artifactoryUrl must be set for avito.internalBuild=true"
            }
            return artifactoryUrl!!
        } else {
            throw IllegalStateException("artifactoryUrl is valid only for avito.internalBuild")
        }
    }

    fun MavenArtifactRepository.isProxy(): Boolean =
        url.toString().startsWith(artifactoryUrl())

    fun MavenArtifactRepository.isMavenLocal(): Boolean =
        name == ArtifactRepositoryContainer.DEFAULT_MAVEN_LOCAL_REPO_NAME

    /**
     * They can be added by 3-rd party plugins or IDE init scripts.
     */
    fun RepositoryHandler.ensureUseOnlyProxies() =
        withType<MavenArtifactRepository> {
            check(this.isProxy() || this.isMavenLocal()) {
                """
            Unexpected maven repository: name = ${this.name}, url=${this.url}.
            You should use proxy repository in $artifactoryUrl.

            If this is technically impossible (init scripts and so on),
            add this repository to the exclusions in this check.
            """
            }
        }

    fun MavenArtifactRepository.artifactoryUrl(
        artifactoryRepositoryName: String,
        repositoryName: String = artifactoryRepositoryName
    ) {
        name = repositoryName
        setUrl("${artifactoryUrl()}/$artifactoryRepositoryName")

        // artifactory is safe behind vpn, but ssl is possible
        // speed not really a factor here, because gradle daemon keeps connections for dependency resolving
        isAllowInsecureProtocol = true
    }

    fun MavenArtifactRepository.setUrlOrProxy(
        artifactoryRepositoryName: String,
        originalRepo: String
    ) {
        if (isInternalBuild) {
            artifactoryUrl(
                artifactoryRepositoryName = artifactoryRepositoryName,
                repositoryName = "Proxy for $artifactoryRepositoryName: $originalRepo"
            )
        } else {
            // Artifactory repo name is a good name/alias, nothing more
            name = artifactoryRepositoryName
            setUrl(originalRepo)
        }
    }

    repositories {
        exclusiveContent {
            forRepositories(
                mavenLocal(),
                if (isInternalBuild) {
                    maven {
                        name = "Artifactory libs-release-local"
                        artifactoryUrl(
                            artifactoryRepositoryName = "libs-release-local",
                        )
                    }
                } else {
                    maven {
                        setUrlOrProxy(
                            artifactoryRepositoryName = "mavenCentral",
                            originalRepo = "https://repo1.maven.org/maven2"
                        )
                    }
                },
            )

            filter {
                includeModuleByRegex("com\\.avito\\.android", ".*")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    setUrlOrProxy(
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
                artifactoryRepositoryName = "gradle-plugins",
                originalRepo = "https://plugins.gradle.org/m2/"
            )
        }
        if (isInternalBuild) {
            ensureUseOnlyProxies()
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
