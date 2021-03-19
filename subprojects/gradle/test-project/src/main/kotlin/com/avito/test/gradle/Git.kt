package com.avito.test.gradle

import com.avito.logger.StubLoggerFactory
import com.avito.utils.ProcessRunner
import java.io.File
import java.security.MessageDigest
import java.util.Random

fun File.git(command: String): String = processRunner().run("git $command").getOrThrow()

fun File.getCommitHash(): String = git("rev-parse HEAD")

fun File.commit(message: String = "changes") {
    git("add --all")
    git("commit --author='test <>' --all --message='${message.escape()}'")
}

private fun File.processRunner(): ProcessRunner {
    return ProcessRunner.Real(
        workingDirectory = this,
        loggerFactory = StubLoggerFactory
    )
}

fun randomCommitHash(): String =
    MessageDigest.getInstance("SHA-1")
        .digest(random.nextLong().toString().toByteArray())
        .fold("") { str, bytes -> str + "%02x".format(bytes) }

private fun String.escape() = replace("\\s+".toRegex()) { "_" }

private val random = Random()
