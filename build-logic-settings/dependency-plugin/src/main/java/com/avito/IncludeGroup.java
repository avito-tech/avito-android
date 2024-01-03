package com.avito;

import org.gradle.api.artifacts.repositories.InclusiveRepositoryContentDescriptor;

public class IncludeGroup implements RepositoryContentFilter {

    private final String group;

    public IncludeGroup(String group) {
        this.group = group;
    }

    @Override
    public void apply(InclusiveRepositoryContentDescriptor filter) {
        filter.includeGroup(group);
    }
}
