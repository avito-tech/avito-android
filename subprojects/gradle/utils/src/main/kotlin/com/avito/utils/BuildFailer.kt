package com.avito.utils

import org.gradle.api.Project
import org.gradle.api.Task

val Task.buildFailer: BuildFailer
    get() = BuildFailer.RealFailer()

val Project.buildFailer: BuildFailer
    get() = BuildFailer.RealFailer()

interface BuildFailer {

    fun failBuild(reason: String)

    class RealFailer : BuildFailer {

        override fun failBuild(reason: String) {
            error(reason)
        }
    }
}
