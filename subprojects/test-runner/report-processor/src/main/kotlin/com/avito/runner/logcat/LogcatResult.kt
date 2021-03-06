package com.avito.runner.logcat

import com.avito.android.Problem

internal sealed class LogcatResult {

    data class Success(val output: String) : LogcatResult()

    data class Unavailable(val reason: Problem) : LogcatResult()
}
