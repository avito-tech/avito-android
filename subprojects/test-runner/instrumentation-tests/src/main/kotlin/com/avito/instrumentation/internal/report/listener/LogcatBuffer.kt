package com.avito.instrumentation.internal.report.listener

import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File

internal interface LogcatBuffer {

    data class Logcat(
        val stdout: List<String>,
        val stderr: List<String>
    )

    fun getStdout(): List<String>

    fun getStderr(): List<String>

    fun getLogs(): Logcat

    fun stop()

    class Impl(
        logcatFile: File,
        readFileFromEnd: Boolean = true
    ) : LogcatBuffer {

        private val stdoutBuffer = mutableListOf<String>()
        private val stderrBuffer = mutableListOf<String>()

        private val tailerListener = object : TailerListenerAdapter() {

            override fun handle(line: String?) {
                if (line != null) {
                    stdoutBuffer.add(line)

                    if (line.contains(" E ") || line.startsWith("E/")) {
                        stderrBuffer.add(line)
                    }
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

        override fun getStdout(): List<String> {
            return stdoutBuffer.toList()
        }

        override fun getStderr(): List<String> {
            return stderrBuffer.toList()
        }

        override fun getLogs(): Logcat {
            return Logcat(
                stdout = stdoutBuffer.toList(),
                stderr = stderrBuffer.toList()
            )
        }
    }
}
