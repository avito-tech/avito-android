package com.avito.android.baseline_profile.internal

import com.avito.android.Result
import com.avito.android.baseline_profile.configuration.SaveProfileToVersionControlSettings
import com.avito.git.GitClient
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import java.io.File
import java.nio.file.Path

internal class BaselineProfileGitClient(
    rootProjectDir: File,
    private val settings: SaveProfileToVersionControlSettings,
    loggerFactory: LoggerFactory,
) {
    private val git = GitClient(rootProjectDir)
    private val logger = loggerFactory.create<BaselineProfileGitClient>()
    private val baselineProfileTagPrefix = "baseline-profile-"

    internal fun isHeadCommitWithProfile(): Boolean {
        val currentBranchName = git.currentBranchName().getOrThrow()
        val profileTag = baselineProfileTagPrefix + currentBranchName

        return git.tagsAtCurrentBranch()
            .map { tags -> tags.contains(profileTag) }
            .rescue {
                logger.warn("Failed to obtain tags of $currentBranchName, ignoring", it)
                Result.Success(false)
            }
            .getOrThrow()
    }

    internal fun commitAndPushProfile(filePath: Path) {
        val branch = git.currentBranchName().getOrThrow()
        val profileTag = baselineProfileTagPrefix + branch

        val hash = git.headHashShort().getOrThrow()
        val details = if (settings.includeDetailsInCommitMessage) " ($branch/$hash)" else ""
        val message = "${settings.commitMessage}$details"

        git.commitSingleFile(filePath, message).getOrThrow()
        if (settings.enableRemoteOperations) {
            git.push(branch).getOrThrow()
        }

        val profileCommitHash = git.headHashFull().getOrThrow()
        logger.warn("Successfully pushed updated profile to $branch (commit hash - $profileCommitHash)")

        updateHeadCommitTag(profileTag)
        logger.warn("Successfully updated profile tag, it is now pointing at $profileCommitHash")
    }

    private fun updateHeadCommitTag(profileTag: String) {
        git.commitTag(profileTag).getOrThrow()

        if (settings.enableRemoteOperations) {
            git.deleteRemoteTag(profileTag).getOrElse {
                logger.warn("Intentionally ignored error while deleting remote tag - it may not be set yet", it)
            }

            git.pushRemoteTag(profileTag)
        }
    }
}
