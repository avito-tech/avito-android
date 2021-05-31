package com.avito.instrumentation.internal.logcat

internal interface LogcatBuffer {

    fun getLogs(): Logcat

    fun stop()
}
