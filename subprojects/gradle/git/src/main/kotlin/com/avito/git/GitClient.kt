package com.avito.git

import com.avito.android.Result
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

public class GitClient(
    private val rootProjectDir: File,
) {
    private val git: Git = Git.create(rootProjectDir)

    public fun currentBranchName(): Result<String> =
        git.tryParseRev("HEAD", abbrevRef = true)

    public fun headHashShort(): Result<String> =
        git.tryParseRev("HEAD", short = true)

    public fun headHashFull(): Result<String> =
        git.tryParseRev("HEAD", short = false)

    public fun tagsAtCurrentBranch(): Result<List<String>> {
        return headHashFull()
            .flatMap { hash -> git.tryParseTags(hash) }
            .map { tags -> tags.split("\n") }
    }

    public fun commitSingleFile(filePath: Path, message: String): Result<Unit> =
        git.add(filePath.relativeTo(rootProjectDir.toPath()).pathString)
            .flatMap {
                git.commit(
                    message = message,
                    allFiles = false
                )
            }

    public fun push(branch: String): Result<Unit> = git.push(branch)

    public fun commitTag(tag: String): Result<Unit> = git.tag(tag, force = true)

    public fun deleteRemoteTag(tag: String): Result<Unit> = git.pushTag(tag, delete = true)

    public fun pushRemoteTag(tag: String): Result<Unit> = git.pushTag(tag)
}
