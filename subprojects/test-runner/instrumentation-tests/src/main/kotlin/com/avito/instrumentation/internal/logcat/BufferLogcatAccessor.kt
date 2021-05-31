package com.avito.instrumentation.internal.logcat

internal class BufferLogcatAccessor(private val logcatBuffer: LogcatBuffer?) : LogcatAccessor {

    override val isAvailable: Boolean
        get() = logcatBuffer?.getLogs()?.output?.isNotEmpty() ?: false

    override fun getLogs(): Logcat {
        return logcatBuffer?.getLogs() ?: Logcat.STUB
    }
}
