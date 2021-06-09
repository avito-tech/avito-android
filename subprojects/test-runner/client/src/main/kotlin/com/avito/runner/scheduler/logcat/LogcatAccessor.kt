package com.avito.runner.scheduler.logcat

internal interface LogcatAccessor {

    fun getLogs(): LogcatResult
}
