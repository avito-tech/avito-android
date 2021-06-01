package com.avito.instrumentation.internal.logcat

internal interface LogcatAccessor {

    fun getLogs(): LogcatResult
}
