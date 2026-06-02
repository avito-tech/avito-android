package com.avito.git

import org.gradle.api.provider.Property

/**
 * Inputs (gradle properties) that determine how git state is resolved. Shared by
 * [GitStateValueSource] and [GitInfoBuildService] so the property wiring lives in a
 * single place — see `Project.configureGitStateParameters`.
 */
public interface GitStateParameters {
    public val strategy: Property<String>
    public val gitBranch: Property<String>
    public val targetBranch: Property<String>
    public val originalCommitHash: Property<String>
}
