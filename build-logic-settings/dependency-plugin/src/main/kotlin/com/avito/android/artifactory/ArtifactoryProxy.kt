package com.avito.android.artifactory

import org.gradle.api.artifacts.repositories.MavenArtifactRepository

const val avitoRepoPrefix = "[AvitoInfra]"

fun MavenArtifactRepository.setUrlOrProxy(
    artifactoryUrl: String?,
    artifactoryRepositoryName: String,
    originalRepo: String
) {
    if (artifactoryUrl.isNullOrBlank()) {
        // it looks strange that artifacatory repo name is here in name of non-artifactory repo
        // it suits here just because artifactory cache repo is a good name/alias, nothing more
        name = "$avitoRepoPrefix $artifactoryRepositoryName"
        setUrl(originalRepo)
    } else {
        artifactoryUrl(
            artifactoryUrl = artifactoryUrl,
            artifactoryRepositoryName = artifactoryRepositoryName,
            repositoryName = "$avitoRepoPrefix Proxy for $artifactoryRepositoryName: $originalRepo"
        )
    }
}

fun MavenArtifactRepository.artifactoryUrl(
    artifactoryUrl: String?,
    artifactoryRepositoryName: String,
    repositoryName: String = "$avitoRepoPrefix $artifactoryRepositoryName"
) {
    name = repositoryName
    setUrl("$artifactoryUrl/$artifactoryRepositoryName")

    // artifactory is safe behind vpn, but ssl is possible
    // speed not really a factor here, because gradle daemon keeps connections for dependency resolving
    isAllowInsecureProtocol = true
}
