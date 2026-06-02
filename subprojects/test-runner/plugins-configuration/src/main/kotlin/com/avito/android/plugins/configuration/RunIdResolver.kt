package com.avito.android.plugins.configuration

import com.avito.reportviewer.model.RunId
import org.gradle.api.provider.Provider

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

    /**
     * Lazy variant of [getRunId]. Wire it into task properties as an unrealized
     * [Provider] so the git read happens at task-execution time and the configuration
     * cache stores the recipe rather than a baked value. See MBSA-2353.
     */
    public fun getRunIdProvider(): Provider<RunId> =
        gitResolver.getGitCommit().map { gitCommitHash ->
            RunId(
                identifier = gitCommitHash,
                buildTypeId = buildEnvResolver.getBuildType()
            )
        }
}
