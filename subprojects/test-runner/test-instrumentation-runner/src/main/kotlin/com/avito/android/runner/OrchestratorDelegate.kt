package com.avito.android.runner

import android.os.Bundle

interface OrchestratorDelegate {

    fun isRealRun(arguments: Bundle): Boolean = !arguments.containsKey(FAKE_ORCHESTRATOR_RUN_ARGUMENT)
}

private const val FAKE_ORCHESTRATOR_RUN_ARGUMENT = "listTestsForOrchestrator"
