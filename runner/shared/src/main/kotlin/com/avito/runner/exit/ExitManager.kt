package com.avito.runner.exit

import org.gradle.api.GradleException

interface ExitManager {
    fun exit(exit: Exit)
}

class GradleExitManager : ExitManager {

    override fun exit(exit: Exit) {
        throw GradleException(exit.message ?: "")
    }
}
