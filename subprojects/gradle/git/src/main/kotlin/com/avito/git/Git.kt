package com.avito.git

import com.avito.android.Result
import java.io.File

public interface Git {

    private val defaultRemote: String
        get() = "origin"

    public fun init(): Result<Unit>

    public fun addAll(): Result<Unit>

    public fun add(filePath: String): Result<Unit>

    public fun commit(message: String, allFiles: Boolean = true, escapeMessage: Boolean = false): Result<Unit>

    public fun checkout(branchName: String, create: Boolean): Result<Unit>

    public fun addRemote(url: String): Result<Unit>

    public fun fetch(remote: String = defaultRemote, commitHash: String?, depth: Int? = null): Result<Unit>

    public fun resetHard(revision: String): Result<Unit>

    public fun push(branchName: String): Result<Unit>

    public fun tag(tagName: String, force: Boolean = false): Result<Unit>

    public fun pushTag(tagName: String, delete: Boolean = false): Result<Unit>

    /**
     * @param abbrevRef Use a non-ambiguous short name of the objects name
     */
    public fun tryParseRev(
        branchName: String,
        abbrevRef: Boolean = false,
        short: Boolean = false,
    ): Result<String>

    public fun tryParseTags(commitHash: String): Result<String>

    public fun log(options: String = "--oneline"): Result<String>

    public fun config(option: String): Result<String>

    public companion object {

        public fun create(
            rootDir: File,
        ): Git = GitImpl(rootDir)
    }
}
