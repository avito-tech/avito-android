package com.avito.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Callable

internal class CLIOutputReaderTask(
    private val stdout: InputStream
) : Callable<String> {

    override fun call(): String {
        val result = StringBuilder()
        val stdoutBuffer = BufferedReader(InputStreamReader(stdout))
        while (true) {
            val line = stdoutBuffer.readLine()
            if (line != null) {
                result.appendLine(line)
            } else {
                break
            }
        }
        return result.toString().trim()
    }
}
