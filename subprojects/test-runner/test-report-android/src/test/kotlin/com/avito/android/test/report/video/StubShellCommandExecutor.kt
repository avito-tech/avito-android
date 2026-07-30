package com.avito.android.test.report.video

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

internal class StubShellCommandExecutor(
    private val output: String = "",
    private val failure: Throwable? = null,
    private val failWhen: (command: String) -> Boolean = { true },
) : ShellCommandExecutor {

    val executedCommands: MutableList<String> = mutableListOf()

    override fun execute(command: String): InputStream {
        executedCommands.add(command)
        failIfNeeded(command)
        return ByteArrayInputStream(output.toByteArray())
    }

    override fun execute(command: String, output: File) {
        executedCommands.add(command)
        failIfNeeded(command)
    }

    private fun failIfNeeded(command: String) {
        val failure = failure ?: return
        if (failWhen(command)) {
            throw failure
        }
    }
}
