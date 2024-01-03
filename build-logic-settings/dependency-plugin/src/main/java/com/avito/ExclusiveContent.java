package com.avito;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.InclusiveRepositoryContentDescriptor;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import java.util.List;

public class ExclusiveContent {

    private final ForRepositories forRepositories;
    private final Filters filters;

    public ExclusiveContent(ForRepositories forRepositories, Filters filters) {
        this.forRepositories = forRepositories;
        this.filters = filters;
    }

    public void apply(RepositoryHandler repositories) {
        repositories.exclusiveContent(exclusiveContentRepository -> {
            exclusiveContentRepository.forRepositories(
                forRepositories.mavenRepositoryFactories
                    .stream()
                    .map(MavenRepositoryFactory::create)
                    .toList()
                    .toArray(MavenArtifactRepository[]::new)
            );
            exclusiveContentRepository.filter(filters::apply);
        });
    }

    static class ForRepositories {
        final List<MavenRepositoryFactory> mavenRepositoryFactories;

        public ForRepositories(List<MavenRepositoryFactory> mavenRepositoryFactories) {
            this.mavenRepositoryFactories = mavenRepositoryFactories;
        }
    }

    static class Filters {
        private final List<RepositoryContentFilter> filters;

        Filters(List<RepositoryContentFilter> filters) {
            this.filters = filters;
        }

        public void apply(InclusiveRepositoryContentDescriptor filter) {
            filters.forEach(repositoryContentFilter -> repositoryContentFilter.apply(filter));
        }
    }
}
