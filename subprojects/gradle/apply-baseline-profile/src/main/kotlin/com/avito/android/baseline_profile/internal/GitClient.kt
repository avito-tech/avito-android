package com.avito.android.baseline_profile.internal

import com.avito.android.Result
import com.avito.git.Git
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

internal class GitClient(
    private val rootProjectDir: File,
    private val extension: SaveProfileToVersionControlExtension,
    loggerFactory: LoggerFactory,
) {
    private val git: Git = Git.create(rootProjectDir)
    private val logger = loggerFactory.create<GitClient>()
    private val baselineProfileTagPrefix = "baseline-profile-"

    internal fun isHeadCommitWithProfile(): Boolean {
        val branch = git.tryParseRev("HEAD", abbrevRef = true).getOrThrow()
        val headHash = git.tryParseRev("HEAD", short = false).getOrThrow()
        val profileTag = baselineProfileTagPrefix + branch

        return git.tryParseTags(headHash)
            .map { tags ->
                tags
                    .split("\n")
                    .contains(profileTag)
            }
            .rescue {
                logger.warn("Failed to obtain tags of $headHash, ignoring", it)
                Result.Success(false)
            }
            .getOrThrow()
    }

    internal fun commitAndPushProfile(filePath: Path) {
        val branch = git.tryParseRev("HEAD", abbrevRef = true).getOrThrow()
        val profileTag = baselineProfileTagPrefix + branch

        commitAndPushFile(filePath, branch)

        val profileCommitHash = git.tryParseRev("HEAD", short = false).getOrThrow()
        logger.warn("Successfully pushed updated profile to $branch (commit hash - $profileCommitHash)")

        updateHeadCommitTag(profileTag)
        logger.warn("Successfully updated profile tag, it is now pointing at $profileCommitHash")
    }

    private fun commitAndPushFile(filePath: Path, branch: String) {
        val hash = git.tryParseRev("HEAD", short = true).getOrThrow()

        git.add(filePath.relativeTo(rootProjectDir.toPath()).pathString).getOrThrow()

        val details = if (extension.includeDetailsInCommitMessage.get()) " ($branch/$hash)" else ""
        git.commit(
            message = "${extension.commitMessage.get()}$details",
            allFiles = false
        ).getOrThrow()

        if (extension.enableRemoteOperations.get()) {
            git.push(branch).getOrThrow()
        }
    }

    private fun updateHeadCommitTag(profileTag: String) {
        git.tag(profileTag, force = true).getOrThrow()

        if (extension.enableRemoteOperations.get()) {
            git.pushTag(profileTag, delete = true).getOrElse {
                logger.warn("Intentionally ignored error while deleting remote tag - it may not be set yet", it)
            }
            git.pushTag(profileTag).getOrThrow()
        }
    }
}
