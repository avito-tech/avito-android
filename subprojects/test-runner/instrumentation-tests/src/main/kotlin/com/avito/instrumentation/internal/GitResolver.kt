package com.avito.instrumentation.internal

import com.avito.git.gitState
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal object GitResolver {

    fun getGitBranch(project: Project): Provider<String> {
        return project.gitState().map { it.currentBranch.name }
    }

    fun getGitCommit(project: Project): Provider<String> {
        return project.gitState().map { it.currentBranch.commit }
    }

    fun getTargetCommit(project: Project): Provider<String> {
        return project.gitState().map { git ->
            requireNotNull(git.targetBranch?.commit) {
                "Target commit is required to find modified tests"
            }
        }
    }
}
