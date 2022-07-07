package com.avito.android.artifactory

import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.withType

fun MavenArtifactRepository.setUrlOrProxy(
    artifactoryUrl: String?,
    artifactoryRepositoryName: String,
    originalRepo: String
) {
    if (artifactoryUrl.isNullOrBlank()) {
        // Artifactory repo name is a good name/alias, nothing more
        name = artifactoryRepositoryName
        setUrl(originalRepo)
    } else {
        artifactoryUrl(
            artifactoryUrl = artifactoryUrl,
            artifactoryRepositoryName = artifactoryRepositoryName,
            repositoryName = "Proxy for $artifactoryRepositoryName: $originalRepo"
        )
    }
}

fun MavenArtifactRepository.artifactoryUrl(
    artifactoryUrl: String?,
    artifactoryRepositoryName: String,
    repositoryName: String = artifactoryRepositoryName
) {
    name = repositoryName
    setUrl("$artifactoryUrl/$artifactoryRepositoryName")

    // artifactory is safe behind vpn, but ssl is possible
    // speed not really a factor here, because gradle daemon keeps connections for dependency resolving
    isAllowInsecureProtocol = true
}

internal fun MavenArtifactRepository.isProxy(artifactoryUrl: String): Boolean =
    url.toString().startsWith(artifactoryUrl)

internal fun MavenArtifactRepository.isMavenLocal(): Boolean =
    name == ArtifactRepositoryContainer.DEFAULT_MAVEN_LOCAL_REPO_NAME

/**
 * They can be added by 3-rd party plugins or IDE init scripts.
 */
fun RepositoryHandler.ensureUseOnlyProxies(artifactoryUrl: String) =
    withType<MavenArtifactRepository> {
        check(this.isProxy(artifactoryUrl) || this.isMavenLocal()) {
            """
            Unexpected maven repository: name = ${this.name}, url=${this.url}.
            You should use proxy repository in $artifactoryUrl.

            If this is technically impossible (init scripts and so on),
            add this repository to the exclusions in this check.
            """
        }
    }
