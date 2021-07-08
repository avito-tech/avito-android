package com.avito.runner.logcat

internal interface LogcatBuffer {

    fun getLogs(): LogcatResult

    fun stop()
}
