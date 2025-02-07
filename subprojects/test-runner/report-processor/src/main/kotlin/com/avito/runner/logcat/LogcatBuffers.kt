package com.avito.runner.logcat

import com.avito.test.model.TestCase

internal interface LogcatBuffers {
    data class Key(
        val testCase: TestCase,
        val executionNumber: Int
    )

    fun create(key: Key, logcatBuffer: LogcatBuffer)
    fun get(key: Key): LogcatBuffer?
    fun destroy(key: Key)

    companion object {
        fun create(logcatDisabled: Boolean): LogcatBuffers = when (logcatDisabled) {
            true -> NoOp()
            false -> Impl()
        }
    }

    private class Impl : LogcatBuffers {

        private val buffers = mutableMapOf<Key, LogcatBuffer>()

        override fun create(key: Key, logcatBuffer: LogcatBuffer) {
            buffers[key] = logcatBuffer
        }

        override fun get(key: Key): LogcatBuffer? {
            return buffers[key]
        }

        override fun destroy(key: Key) {
            buffers.remove(key)?.stop()
        }
    }

    private class NoOp : LogcatBuffers {
        private val emptyBuffer = object : LogcatBuffer {
            override fun getLogs(): LogcatResult = LogcatResult.Success("Logcat is disabled")

            override fun stop() {
                // no-op
            }
        }

        override fun create(key: Key, logcatBuffer: LogcatBuffer) {
            // no-op
        }

        override fun get(key: Key): LogcatBuffer = emptyBuffer

        override fun destroy(key: Key) {
            // no-op
        }
    }
}
