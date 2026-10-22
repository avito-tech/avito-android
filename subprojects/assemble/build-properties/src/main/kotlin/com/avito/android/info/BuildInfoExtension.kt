package com.avito.android.info

public open class BuildInfoExtension {
    public var gitCommit: GitValue = GitValue.FromGit
    public var gitBranch: GitValue = GitValue.FromGit
    public var buildNumber: String? = null
}

public sealed interface GitValue {

    /**
     * Resolved from git at task execution time, so a new commit doesn't invalidate the configuration cache
     */
    public data object FromGit : GitValue

    /**
     * Written as an empty value; git is not read
     */
    public data object Omitted : GitValue

    public data class Explicit(val value: String) : GitValue
}
