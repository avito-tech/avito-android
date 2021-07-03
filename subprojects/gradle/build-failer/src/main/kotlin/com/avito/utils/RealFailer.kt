package com.avito.utils

import com.avito.android.Problem
import com.avito.android.asRuntimeException

internal class RealFailer : BuildFailer {

    override fun failBuild(problem: Problem) {
        throw problem.asRuntimeException()
    }

    override fun failBuild(message: String) {
        throw IllegalStateException(message)
    }

    override fun failBuild(message: String, cause: Throwable) {
        throw IllegalStateException(message, cause)
    }
}
