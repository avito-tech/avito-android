package com.avito.runner.scheduler.logcat

internal interface LogcatBuffer {

    fun getLogs(): LogcatResult

    fun stop()
}
