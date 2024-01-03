package com.avito;

import org.gradle.api.artifacts.repositories.InclusiveRepositoryContentDescriptor;

public class IncludeGroupByRegex implements RepositoryContentFilter {

    private final String regex;

    public IncludeGroupByRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public void apply(InclusiveRepositoryContentDescriptor filter) {
        filter.includeGroupByRegex(regex);
    }
}
