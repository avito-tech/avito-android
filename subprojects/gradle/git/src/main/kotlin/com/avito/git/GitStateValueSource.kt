package com.avito.git

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.File
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
        val workingDir: DirectoryProperty
    }

    private val workingDir: File
        get() = parameters.workingDir.get().asFile

    override fun obtain(): GitState {
        val gradleGit = GradleGit(execOperations, workingDir)

        return when (val strategy = parameters.strategy.get()) {
            "local" -> GitLocalState.from(
                    git = gradleGit,
                    parameters.targetBranch.orNull,
                )

            "env" -> GitStateFromEnvironment.from(
                git = gradleGit,
                gitBranch = parameters.gitBranch.get(),
                targetBranch = parameters.targetBranch.orNull,
                originalCommitHash = parameters.originalCommitHash.orNull,
            )

            else -> throw RuntimeException("Unknown git state strategy: $strategy")
        }
    }
}
