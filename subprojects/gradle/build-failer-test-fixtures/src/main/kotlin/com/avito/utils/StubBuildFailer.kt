package com.avito.utils

import com.avito.android.Problem

class StubBuildFailer : BuildFailer {

    var lastProblem: Problem? = null
    var lastReason: String? = null
    var lastCause: Throwable? = null

    override fun failBuild(problem: Problem) {
        lastProblem = problem
    }

    override fun failBuild(message: String) {
        lastReason = message
    }

    override fun failBuild(message: String, cause: Throwable) {
        lastReason = message
        lastCause = cause
    }
}
