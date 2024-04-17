package com.avito.git

import com.avito.android.Result
import com.avito.git.executor.GitExecutor

internal class GitImpl(
    private val executor: GitExecutor,
) : Git {

    override fun init(): Result<Unit> = git("init").map { }

    override fun addAll(): Result<Unit> = git("add --all").map { }

    override fun add(filePath: String): Result<Unit> =
        git("add $filePath").map { }

    override fun commit(
        message: String,
        allFiles: Boolean,
        escapeMessage: Boolean
    ): Result<Unit> {
        val allOption = if (allFiles) " --all" else ""
        val msg = if (escapeMessage) escapeGitMessage(message) else message
        return git("commit$allOption --message='$msg'").map { }
    }

    override fun checkout(branchName: String, create: Boolean): Result<Unit> =
        git("checkout ${if (create) "-b" else ""} $branchName").map { }

    override fun addRemote(url: String): Result<Unit> = git("remote add origin $url").map { }

    override fun fetch(remote: String, commitHash: String?, depth: Int?): Result<Unit> =
        git("fetch $remote ${if (depth != null) "--depth=$depth" else ""} $commitHash").map { }

    override fun resetHard(revision: String): Result<Unit> = git("reset --hard $revision").map { }

    override fun config(option: String): Result<String> = git("config $option")

    override fun push(branchName: String): Result<Unit> =
        git("push origin $branchName").map { }

    override fun tag(tagName: String, force: Boolean): Result<Unit> {
        val forceOption = if (force) " -f" else ""
        return git("tag$forceOption $tagName").map { }
    }

    override fun pushTag(tagName: String, delete: Boolean): Result<Unit> {
        val deleteOption = if (delete) " --delete" else ""
        return git("push origin$deleteOption refs/tags/$tagName").map { }
    }

    override fun tryParseRev(
        branchName: String,
        abbrevRef: Boolean,
        short: Boolean
    ): Result<String> {
        val abbrevRefOption = if (abbrevRef) " --abbrev-ref" else ""
        val shortOption = if (short) " --short" else ""
        return git("rev-parse$abbrevRefOption$shortOption $branchName")
            .recover { error ->
                throw IllegalStateException(
                    "Can't get revision for $branchName",
                    error
                )
            }
    }

    override fun tryParseTags(commitHash: String): Result<String> {
        return git("tag --contains $commitHash")
            .recover { error ->
                throw IllegalStateException(
                    "Can't get tags list for $commitHash",
                    error
                )
            }
    }

    override fun log(options: String): Result<String> {
        return git("log $options")
            .recover { error ->
                throw IllegalStateException(
                    "Can't get git log",
                    error
                )
            }
    }

    private fun git(command: String): Result<String> = executor.git(command)

    private fun escapeGitMessage(message: String) = message.replace("\\s+".toRegex()) { "_" }
}
