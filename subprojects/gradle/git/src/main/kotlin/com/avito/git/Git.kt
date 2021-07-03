package com.avito.git

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import java.io.File

public interface Git {

    private val defaultRemote: String
        get() = "origin"

    public fun init(): Result<Unit>

    public fun addAll(): Result<Unit>

    public fun commit(message: String): Result<Unit>

    public fun checkout(branchName: String, create: Boolean): Result<Unit>

    public fun addRemote(url: String): Result<Unit>

    public fun fetch(remote: String = defaultRemote, commitHash: String?, depth: Int? = null): Result<Unit>

    public fun resetHard(revision: String): Result<Unit>

    /**
     * @param abbrevRef Use a non-ambiguous short name of the objects name
     */
    public fun tryParseRev(branchName: String, abbrevRef: Boolean = false): Result<String>

    public fun config(option: String): Result<String>

    public companion object {

        public fun create(
            rootDir: File,
            loggerFactory: LoggerFactory
        ): Git = GitImpl(rootDir, loggerFactory)
    }
}
