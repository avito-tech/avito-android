package com.avito.android

import org.gradle.api.provider.Property

public abstract class ChangedTestsFinderExtension {

    /**
     * Optional explicit override. When unset, [FindChangedTestsTask] resolves
     * the target commit from `GitInfoBuildService` at execution time.
     */
    public abstract val targetCommit: Property<String>
}
