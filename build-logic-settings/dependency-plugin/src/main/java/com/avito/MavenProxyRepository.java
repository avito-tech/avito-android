package com.avito;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public class MavenProxyRepository implements MavenRepositoryFactory {
    private final RepositoryHandler repositories;
    private final String artifactoryUrl;
    private final String repoName;

    public MavenProxyRepository(
        RepositoryHandler repositories,
        String artifactoryUrl,
        String repoName
    ) {
        this.repositories = repositories;
        this.artifactoryUrl = artifactoryUrl;
        this.repoName = repoName;
    }

    @Override
    public MavenArtifactRepository create() {
        return repositories.maven(repo -> {
            repo.setName(repoName);
            repo.setUrl(artifactoryUrl + "/" + repoName);
            // artifactory is safe behind vpn, but ssl is possible
            // speed not really a factor here, because gradle daemon keeps connections for dependency resolving
            repo.setAllowInsecureProtocol(true);
        });
    }
}
