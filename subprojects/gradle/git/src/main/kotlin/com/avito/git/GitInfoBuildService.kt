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
     * Like [getGitState] but returns null instead of throwing when git is unavailable
     * (no repo, detached/unborn HEAD, shallow clone missing the ref). Preserves the
     * nullable contract of the legacy `gitStateProvider().orNull` for callers that
     * degrade gracefully when git can't be read.
     */
    public fun getGitStateOrNull(): GitState? = runCatching { cached }.getOrNull()

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
