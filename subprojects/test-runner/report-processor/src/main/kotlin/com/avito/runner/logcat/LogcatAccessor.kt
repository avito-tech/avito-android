package com.avito.runner.logcat

internal interface LogcatAccessor {

    fun getLogs(): LogcatResult
}
