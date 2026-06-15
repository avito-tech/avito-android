package com.avito.android.plugins.configuration

import com.avito.reportviewer.model.RunId
import org.gradle.api.provider.Provider

public class RunIdResolver(
    private val gitResolver: GitResolver,
    private val buildEnvResolver: BuildEnvResolver,
) {

    public fun getRunId(): RunId {

        if (buildEnvResolver.isLocal()) {
            // Local builds: a stable, git-independent runId. getRunId() is read eagerly at
            // configuration time by some callers (e.g. AGP testInstrumentationRunnerArguments),
            // so reading git here would invalidate the configuration cache on every commit /
            // branch switch. The identifier is local-only report metadata. See MBSA-2360.
            return RunId(
                identifier = "local",
                buildTypeId = buildEnvResolver.getBuildType()
            )
        }

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
