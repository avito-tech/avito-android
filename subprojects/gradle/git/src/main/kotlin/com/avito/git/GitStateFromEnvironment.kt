package com.avito.git

import com.avito.git.executor.InProcessExecutor
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project

internal object GitStateFromEnvironment {

    fun from(project: Project): GitState {
        val gitBranch: String = project.getMandatoryStringProperty("gitBranch")
        val targetBranch: String? = project.getOptionalStringProperty("targetBranch")
        val originalCommitHash: String? = project.getOptionalStringProperty("originalCommitHash")
        val git = GitImpl(
            executor = InProcessExecutor(project.rootDir),
        )
        return from(
            git,
            gitBranch,
            targetBranch,
            originalCommitHash
        )
    }

    fun from(
        git: Git,
        gitBranch: String,
        targetBranch: String?,
        originalCommitHash: String?
    ): GitState {

        @Suppress("NAME_SHADOWING")
        val gitBranch: String = gitBranch.asBranchWithoutOrigin()

        @Suppress("NAME_SHADOWING")
        val targetBranch: String? = targetBranch?.asBranchWithoutOrigin()

        val gitCommit = git.tryParseRev("HEAD").getOrThrow()

        val currentBranch = Branch(
            name = gitBranch,
            commit = gitCommit
        )

        val originalBranch = Branch(
            name = gitBranch,
            commit = originalCommitHash ?: gitCommit
        )

        var target: Branch? = null

        if (!targetBranch.isNullOrBlank()) {
            val remoteTargetBranch: String = targetBranch.asOriginBranch()

            // Сначала ищем ветку из remote если ее нет, ищем локальную ветку
            git.tryParseRev(remoteTargetBranch)
                .rescue { git.tryParseRev(targetBranch) }
                .onSuccess { targetCommit ->
                    target = Branch(
                        name = targetBranch,
                        commit = targetCommit
                    )
                }
        }

        return GitState(
            originalBranch = originalBranch,
            currentBranch = currentBranch,
            targetBranch = target,
            defaultBranch = "develop",
            false,
        )
    }
}
