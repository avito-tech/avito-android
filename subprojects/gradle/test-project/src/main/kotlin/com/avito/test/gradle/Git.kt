package com.avito.test.gradle

import com.avito.utils.runCommand
import java.io.File
import java.security.MessageDigest
import java.util.Random

fun File.git(command: String): String {
    return runCommand(
        command = "git $command",
        workingDirectory = this
    ).get()
}

fun File.getCommitHash(): String {
    return runCommand(
        command = "git rev-parse HEAD",
        workingDirectory = this
    ).get()
}

fun File.commit(message: String = "changes") {
    runCommand(
        command = "git add --all",
        workingDirectory = this
    ).get()
    runCommand(
        command = "git commit --author='test <>' --all --message='${message.escape()}'",
        workingDirectory = this
    ).get()
}

fun randomCommitHash(): String =
    MessageDigest.getInstance("SHA-1")
        .digest(random.nextLong().toString().toByteArray())
        .fold("") { str, bytes -> str + "%02x".format(bytes) }

private fun String.escape() = replace("\\s+".toRegex()) { "_" }

private val random = Random()
