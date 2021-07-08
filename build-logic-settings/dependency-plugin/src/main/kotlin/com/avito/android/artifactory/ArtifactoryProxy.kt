package com.avito.android.artifactory

import org.gradle.api.artifacts.repositories.MavenArtifactRepository

fun MavenArtifactRepository.setUrlOrProxy(artifactoryUrl: String?, repositoryName: String, originalRepo: String) {
    if (artifactoryUrl.isNullOrBlank()) {
        name = repositoryName
        setUrl(originalRepo)
    } else {
        name = "Proxy for $repositoryName: $originalRepo"
        artifactoryUrl(artifactoryUrl, repositoryName)
    }
}

private fun MavenArtifactRepository.artifactoryUrl(artifactoryUrl: String?, repositoryName: String) {
    setUrl("$artifactoryUrl/$repositoryName")
    isAllowInsecureProtocol = true
}
