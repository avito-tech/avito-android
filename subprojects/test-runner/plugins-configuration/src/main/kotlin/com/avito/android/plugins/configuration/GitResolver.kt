package com.avito.android.plugins.configuration

import com.avito.git.GitState
import org.gradle.api.provider.Provider

public class GitResolver(private val gitState: Provider<GitState>) {

    public fun getGitBranch(): Provider<String> {
        return gitState.map { it.currentBranch.name }
    }

    public fun getGitCommit(): Provider<String> {
        return gitState.map { it.currentBranch.commit }
    }
}
