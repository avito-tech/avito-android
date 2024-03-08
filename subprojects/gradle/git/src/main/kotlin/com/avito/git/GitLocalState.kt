package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project

internal object GitLocalState {

    fun from(project: Project): GitState {
        return from(
            GitImpl(project.rootDir),
            project.getOptionalStringProperty("targetBranch")
        )
    }

    fun from(
        git: Git,
        targetBranch: String?
    ): GitState {
        @Suppress("NAME_SHADOWING")
        val targetBranch = targetBranch?.asBranchWithoutOrigin()

        val gitBranch: String = git.tryParseRev("HEAD", abbrevRef = true).getOrThrow()
        val gitCommit: String = git.tryParseRev("HEAD", abbrevRef = false).getOrThrow()

        val currentBranch = Branch(
            name = gitBranch,
            commit = gitCommit
        )

        val originalBranch: Branch = currentBranch

        var target: Branch? = null

        if (!targetBranch.isNullOrBlank()) {

            git.tryParseRev(targetBranch)
                .onSuccess { targetCommit ->
                    target = Branch(
                        name = targetBranch,
                        commit = targetCommit
                    )
                }
        }
        return GitState(
            originalBranch,
            currentBranch,
            target,
            "develop",
            true,
        )
    }
}
