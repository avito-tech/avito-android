package com.avito.instrumentation.internal.logcat

import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File

internal class TailingLogcatBuffer(
    logcatFile: File,
    readFileFromEnd: Boolean = true
) : LogcatBuffer {

    private val buffer = mutableListOf<String>()

    private val tailerListener = object : TailerListenerAdapter() {

        override fun handle(line: String?) {
            if (line != null) {
                buffer.add(line)
            }
        }
    }

    private val frequencyMs = 100L

    private val tailer = Tailer.create(
        logcatFile,
        tailerListener,
        frequencyMs,
        readFileFromEnd
    )

    override fun stop() {
        tailer.stop()
    }

    override fun getLogs(): Logcat {
        return Logcat(
            output = buffer.joinToString(separator = "\n")
        )
    }
}
