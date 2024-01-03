package com.avito;

import org.gradle.api.artifacts.repositories.InclusiveRepositoryContentDescriptor;

public class IncludeModuleByRegex implements RepositoryContentFilter {

    private final String groupRegex;
    private final String moduleNameRegex;

    public IncludeModuleByRegex(String groupRegex, String moduleNameRegex) {
        this.groupRegex = groupRegex;
        this.moduleNameRegex = moduleNameRegex;
    }

    @Override
    public void apply(InclusiveRepositoryContentDescriptor filter) {
        filter.includeModuleByRegex(groupRegex, moduleNameRegex);
    }
}
