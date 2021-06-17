package com.avito.utils

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import org.gradle.api.Project

@Suppress("unused")
val Project.buildFailer: BuildFailer
    get() = BuildFailer.RealFailer()

interface BuildFailer {

    fun failBuild(
        problem: Problem
    )

    fun failBuild(
        message: String
    )

    fun failBuild(
        message: String,
        cause: Throwable
    )

    class RealFailer : BuildFailer {

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
}
