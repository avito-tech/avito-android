package com.avito.git

import com.avito.git.executor.GradleCompatibleExecutor
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * CC-safe alternative to [GitStateValueSource]. Tasks declare it via
 * `@get:ServiceReference(GitInfoBuildService.NAME)` and call [getGitState]
 * inside `@TaskAction` — never at configuration time.
 *
 * See MBSA-2353.
 */
public abstract class GitInfoBuildService : BuildService<GitInfoBuildService.Params> {

    public interface Params : BuildServiceParameters, GitStateParameters

    @get:Inject
    protected abstract val execOperations: ExecOperations

    private val cached: GitState by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { compute() }

    public fun getGitState(): GitState = cached

    /**
     * Like [getGitState] but never throws: returns [GitStateResult.Available] with the state, or
     * [GitStateResult.Unavailable] carrying the cause when git can't be read (no repo, detached /
     * unborn HEAD, shallow clone missing the ref, unknown strategy). Callers degrade gracefully
     * while keeping access to the actual reason instead of a contextless null.
     */
    public fun getGitStateResult(): GitStateResult =
        runCatching { cached }.fold(
            onSuccess = { GitStateResult.Available(it) },
            onFailure = { GitStateResult.Unavailable(it) },
        )

    @Deprecated(
        "Ambiguous null collapses every failure mode. Use getGitStateResult() to handle the cause.",
        ReplaceWith("getGitStateResult()"),
    )
    public fun getGitStateOrNull(): GitState? =
        (getGitStateResult() as? GitStateResult.Available)?.state

    private fun compute(): GitState {
        val git = GitImpl(executor = GradleCompatibleExecutor(execOperations))
        return when (val strategy = parameters.strategy.get()) {
            "local" -> GitLocalState.from(
                git = git,
                targetBranch = parameters.targetBranch.orNull,
            )

            "env" -> GitStateFromEnvironment.from(
                git = git,
                gitBranch = parameters.gitBranch.get(),
                targetBranch = parameters.targetBranch.orNull,
                originalCommitHash = parameters.originalCommitHash.orNull,
            )

            else -> throw RuntimeException(
                "Unknown git state strategy for GitInfoBuildService: '$strategy'. " +
                    "Supported: 'local', 'env'."
            )
        }
    }

    public companion object {
        public const val NAME: String = "avitoGitInfo"
    }
}
