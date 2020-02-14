package com.avito.utils

import org.gradle.api.Project

val Project.buildFailer: BuildFailer
    get() = BuildFailer.RealFailer()

interface BuildFailer {

    fun failBuild(
        message: String,
        cause: Throwable
    )

    class RealFailer : BuildFailer {

        override fun failBuild(message: String, cause: Throwable) {
            throw IllegalStateException(message, cause)
        }
    }
}
