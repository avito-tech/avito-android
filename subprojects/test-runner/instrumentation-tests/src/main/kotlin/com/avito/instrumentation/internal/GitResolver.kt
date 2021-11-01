package com.avito.instrumentation.internal

import com.avito.git.GitState
import org.gradle.api.provider.Provider

internal class GitResolver(private val gitState: Provider<GitState>) {

    fun getGitBranch(): Provider<String> {
        return gitState.map { it.currentBranch.name }
    }

    fun getGitCommit(): Provider<String> {
        return gitState.map { it.currentBranch.commit }
    }
}
