package com.avito.instrumentation.internal.logcat

import com.avito.android.Problem

internal class BufferLogcatAccessor(private val logcatBuffer: LogcatBuffer?) : LogcatAccessor {

    override fun getLogs(): LogcatResult {
        return logcatBuffer?.getLogs() ?: LogcatResult.Unavailable(
            Problem(
                shortDescription = "No logcatBuffer allocated for test execution",
                context = "BufferLogcatAccessor, accessing LogcatResult",
                because = "It's unexpected, definitely a bug: means LogcatBuffer not assigned on testStart"
            )
        )
    }
}
