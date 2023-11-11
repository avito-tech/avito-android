package com.avito.android.plugins.configuration

import com.avito.reportviewer.model.RunId

public class RunIdResolver(
    private val gitResolver: GitResolver,
    private val buildEnvResolver: BuildEnvResolver,
) {

    public fun getRunId(): RunId {

        val gitCommitHash = checkNotNull(gitResolver.getGitCommit().orNull) {
            "Failed to create runId. Git commit is null"
        }

        return RunId(
            identifier = gitCommitHash,
            buildTypeId = buildEnvResolver.getBuildType()
        )
    }
}
