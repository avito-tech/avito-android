package com.avito.impact.fallback

import com.avito.git.GitLocalState
import com.avito.git.GitState
import com.avito.impact.configuration.isBranchProtected
import com.avito.impact.plugin.ImpactAnalysisExtension
import com.google.common.annotations.VisibleForTesting
import org.gradle.api.provider.Provider

sealed class IsAnalysisNeededResult {
    object Run : IsAnalysisNeededResult()
    class Skip(val reason: String) : IsAnalysisNeededResult()
}

@VisibleForTesting
fun isAnalysisNeeded(
    config: ImpactAnalysisExtension,
    gitState: Provider<GitState>
): IsAnalysisNeededResult {

    if (config.skipAnalysis) {
        return IsAnalysisNeededResult.Skip("skipAnalysis=true in plugin config")
    }

    val git: GitState = gitState.orNull
        ?: return IsAnalysisNeededResult.Skip("impossible to get diff, git is not available on host machine")

    val currentBranch = git.currentBranch
    if (currentBranch.name.isBlank()) {
        return IsAnalysisNeededResult.Skip("can't determine current branch")
    }

    val targetBranch = git.targetBranch
    if (targetBranch == null || targetBranch.name.isBlank()) {
        return IsAnalysisNeededResult.Skip("can't determine target branch")
    }

    if (targetBranch.commit.isBlank()) {
        return IsAnalysisNeededResult.Skip("can't determine target commit")
    }

    if (isBranchProtected(targetBranch.name, config.protectedBranches)
        || isBranchProtected(currentBranch.name, config.protectedBranches)
    ) {
        return IsAnalysisNeededResult.Skip(
            "branch is protected by plugin rules [${config.protectedBranches}]" +
                "\ncurrentBranch: $currentBranch; targetBranch: $targetBranch"
        )
    }

    if (targetBranch.name == currentBranch.name && (git !is GitLocalState)) {
        return IsAnalysisNeededResult.Skip("running on target branch $targetBranch")
    }

    return IsAnalysisNeededResult.Run
}
