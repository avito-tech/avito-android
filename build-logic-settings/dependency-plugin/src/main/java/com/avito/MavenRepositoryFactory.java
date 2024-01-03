package com.avito;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public interface MavenRepositoryFactory {
    public MavenArtifactRepository create();

    static MavenRepositoryFactory createFactory(
        RepositoryHandler repositories,
        String artifactoryUrl,
        String repoName,
        String originalRepoUrl
    ) {
        if (artifactoryUrl == null || artifactoryUrl.isEmpty() || artifactoryUrl.isBlank()) {
            return new SimpleMavenRepository(repositories, originalRepoUrl, repoName);
        } else {
            return new MavenProxyRepository(repositories, artifactoryUrl, repoName);
        }
    }
}
