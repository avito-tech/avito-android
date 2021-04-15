package com.avito.instrumentation.internal.report.listener

import com.avito.runner.service.model.TestCase

internal class LogcatBuffers {

    data class Key(
        val testCase: TestCase,
        val executionNumber: Int
    )

    private val buffers = mutableMapOf<Key, LogcatBuffer>()

    fun create(key: Key, logcatBuffer: LogcatBuffer) {
        buffers[key] = logcatBuffer
    }

    fun get(key: Key): LogcatBuffer? {
        return buffers[key]
    }

    fun destroy(key: Key) {
        buffers.remove(key)?.stop()
    }
}
