package com.avito;

import org.gradle.api.artifacts.repositories.InclusiveRepositoryContentDescriptor;

public interface RepositoryContentFilter {
    public void apply(InclusiveRepositoryContentDescriptor filter);
}
