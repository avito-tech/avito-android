package com.avito.instrumentation.internal.logcat

import com.avito.android.Result

internal class ResultLogcatAccessor(val result: Result<String>) : LogcatAccessor {

    override val isAvailable: Boolean
        get() = result.isSuccess()

    override fun getLogs(): Logcat {
        return result.map { Logcat(it) }.getOrElse { Logcat.STUB }
    }
}
