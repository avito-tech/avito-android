package com.avito.utils

import com.avito.android.Problem

public class StubBuildFailer : BuildFailer {

    public var lastProblem: Problem? = null
    public var lastReason: String? = null
    public var lastCause: Throwable? = null

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
