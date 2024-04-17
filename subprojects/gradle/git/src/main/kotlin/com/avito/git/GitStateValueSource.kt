package com.avito.git

import com.avito.git.executor.GradleCompatibleExecutor
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * https://docs.gradle.org/8.6/userguide/configuration_cache.html#config_cache:requirements:external_processes
 */
internal abstract class GitStateValueSource : ValueSource<GitState, GitStateValueSource.Params> {

    @get:Inject
    abstract val execOperations: ExecOperations

    interface Params : ValueSourceParameters {
        val strategy: Property<String>
        val gitBranch: Property<String>
        val targetBranch: Property<String>
        val originalCommitHash: Property<String>
    }

    override fun obtain(): GitState {
        val git = GitImpl(executor = GradleCompatibleExecutor(
            execOperations = execOperations,
        ))

        return when (val strategy = parameters.strategy.get()) {
            "local" -> GitLocalState.from(
                    git = git,
                    parameters.targetBranch.orNull,
                )

            "env" -> GitStateFromEnvironment.from(
                git = git,
                gitBranch = parameters.gitBranch.get(),
                targetBranch = parameters.targetBranch.orNull,
                originalCommitHash = parameters.originalCommitHash.orNull,
            )

            else -> throw RuntimeException("Unknown git state strategy: $strategy")
        }
    }
}
