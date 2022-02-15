package ru.avito.image_builder.internal.process

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Callable
import java.util.logging.Logger

internal class CLIOutputReaderTask(
    private val stdout: InputStream
) : Callable<String> {

    private val log = Logger.getLogger(this::class.java.simpleName)

    override fun call(): String {
        val result = StringBuilder()
        val stdoutBuffer = BufferedReader(InputStreamReader(stdout))
        while (true) {
            val line = stdoutBuffer.readLine()
            if (line != null) {
                log.info(line)
                result.appendLine(line)
            } else {
                break
            }
        }
        return result.toString().trim()
    }
}
