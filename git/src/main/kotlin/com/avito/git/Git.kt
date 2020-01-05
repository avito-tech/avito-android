package com.avito.git

import com.avito.utils.runCommand
import org.funktionale.tries.Try
import java.io.File

interface Git {

    val defaultRemote: String
        get() = "origin"

    fun init(): Try<Unit>

    fun addAll(): Try<Unit>

    fun commit(message: String): Try<Unit>

    fun checkout(branchName: String, create: Boolean): Try<Unit>

    fun addRemote(url: String): Try<Unit>

    fun fetch(remote: String = defaultRemote, commitHash: String?, depth: Int? = null): Try<Unit>

    fun resetHard(revision: String): Try<Unit>

    /**
     * @param abbrevRef Use a non-ambiguous short name of the objects name
     */
    fun tryParseRev(branchName: String, abbrevRef: Boolean = false): Try<String>

    fun config(option: String): Try<String>

    class Impl(
        private val dir: File,
        private val logger: (String) -> Unit = {}
    ) : Git {

        override fun init(): Try<Unit> = git("init").map { Unit }

        override fun addAll(): Try<Unit> = git("add --all").map { Unit }

        override fun commit(message: String): Try<Unit> =
            git("commit --author='test <>' --all --message='${escapeGitMessage(message)}'").map { Unit }

        override fun checkout(branchName: String, create: Boolean): Try<Unit> =
            git("checkout ${if (create) "-b" else ""} $branchName").map { Unit }

        override fun addRemote(url: String): Try<Unit> = git("remote add origin $url").map { Unit }

        override fun fetch(remote: String, commitHash: String?, depth: Int?): Try<Unit> =
            git("fetch $remote ${if (depth != null) "--depth=$depth" else ""} $commitHash").map { Unit }

        override fun resetHard(revision: String): Try<Unit> = git("reset --hard $revision").map { Unit }

        override fun config(option: String): Try<String> = git("config $option")

        override fun tryParseRev(branchName: String, abbrevRef: Boolean): Try<String> {
            val abbrevRefOption = if (abbrevRef) "--abbrev-ref" else ""
            return git("rev-parse $abbrevRefOption $branchName")
                .rescue { error ->
                    throw IllegalStateException(
                        "Can't get revision for $branchName",
                        error
                    )
                }
        }

        private fun git(command: String): Try<String> {
            val calledFrom = Thread.currentThread().stackTrace[3]
            logger("GIT running: git $command (${calledFrom.fileName}:${calledFrom.lineNumber})")

            return runCommand(
                command = "git $command",
                workingDirectory = dir
            )
                .apply {
                    onSuccess { result -> logger("GIT result: $result") }
                    onFailure { error -> logger("GIT error: ${error.message}") }
                }
        }

        private fun escapeGitMessage(message: String) = message.replace("\\s+".toRegex()) { "_" }
    }
}
