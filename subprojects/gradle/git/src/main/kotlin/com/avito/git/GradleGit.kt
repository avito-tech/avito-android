package com.avito.git

import com.avito.android.Result
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset

/**
 * Could be used only in [com.avito.git.GitStateValueSource]
 *
 * Implement all methods and replace [GitImpl] in places where configuration cache is broken
 */
internal class GradleGit(
    private val execOperations: ExecOperations,
    private val workingDir: File,
) : Git {

    override fun init(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun addAll(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun add(filePath: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun commit(message: String, allFiles: Boolean, escapeMessage: Boolean): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun checkout(branchName: String, create: Boolean): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun addRemote(url: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun fetch(remote: String, commitHash: String?, depth: Int?): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun resetHard(revision: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun push(branchName: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun tag(tagName: String, force: Boolean): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun pushTag(tagName: String, delete: Boolean): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun tryParseRev(branchName: String, abbrevRef: Boolean, short: Boolean): Result<String> {
        val abbrevRefOption = if (abbrevRef) "--abbrev-ref" else ""
        val shortOption = if (short) "--short" else ""
        val args = arrayOf(
            "git",
            "rev-parse",
            abbrevRefOption,
            shortOption,
            branchName
        ).filter { it.isNotEmpty() }
        return Result.tryCatch {
            val output = ByteArrayOutputStream()
            execOperations.exec {
                it.workingDir = workingDir
                it.commandLine(args)
                it.standardOutput = output
            }.assertNormalExitValue()
            String(output.toByteArray(), Charset.defaultCharset()).trim()
        }.rescue { error ->
            throw RuntimeException("Cant execute $args", error)
        }
    }

    override fun tryParseTags(commitHash: String): Result<String> {
        TODO("Not yet implemented")
    }

    override fun log(options: String): Result<String> {
        TODO("Not yet implemented")
    }

    override fun config(option: String): Result<String> {
        TODO("Not yet implemented")
    }
}
