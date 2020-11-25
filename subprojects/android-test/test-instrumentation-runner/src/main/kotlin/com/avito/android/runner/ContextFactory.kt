package com.avito.android.runner

import android.os.Bundle

interface ContextFactory {
    fun create(arguments: Bundle): Context?

    abstract class Default : ContextFactory {

        private val Bundle.isRealRun: Boolean
            get() = !containsKey(FAKE_ORCHESTRATOR_RUN_ARGUMENT)

        final override fun create(arguments: Bundle): Context? =
            if (arguments.isRealRun) {
                createIfRealRun(arguments)
            } else null

        abstract fun createIfRealRun(arguments: Bundle): Context
    }

    companion object {
        // todo make internal after full migration
        const val FAKE_ORCHESTRATOR_RUN_ARGUMENT = "listTestsForOrchestrator"
    }
}
