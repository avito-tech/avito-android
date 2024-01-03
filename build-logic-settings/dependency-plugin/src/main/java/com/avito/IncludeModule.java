package com.avito;

import org.gradle.api.artifacts.repositories.InclusiveRepositoryContentDescriptor;

public class IncludeModule implements RepositoryContentFilter {

    private final String group;
    private final String moduleName;

    public IncludeModule(String group, String moduleName) {
        this.group = group;
        this.moduleName = moduleName;
    }

    @Override
    public void apply(InclusiveRepositoryContentDescriptor filter) {
        filter.includeModule(group, moduleName);
    }
}
