package com.avito;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public class SimpleMavenRepository implements MavenRepositoryFactory {
    private final RepositoryHandler repositories;
    private final String url;
    private final String name;

    public SimpleMavenRepository(RepositoryHandler repositories, String url, String name) {
        this.repositories = repositories;
        this.url = url;
        this.name = name;
    }

    @Override
    public MavenArtifactRepository create() {
        return repositories.maven(maven -> {
            maven.setName(name);
            maven.setUrl(url);
        });
    }
}
