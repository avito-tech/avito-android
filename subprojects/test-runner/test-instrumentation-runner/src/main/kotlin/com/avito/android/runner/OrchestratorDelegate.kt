package com.avito.android.runner

import android.os.Bundle
import androidx.test.internal.runner.RunnerArgsAccessor

interface OrchestratorDelegate {

    @Deprecated(
        message = "Use isFakeOrchestratorRun(Bundle) instead",
        replaceWith = ReplaceWith("!isFakeOrchestratorRun(arguments)")
    )
    fun isRealRun(arguments: Bundle): Boolean =
        !isFakeOrchestratorRun(arguments)

    fun isFakeOrchestratorRun(arguments: Bundle): Boolean =
        arguments.containsKey(RunnerArgsAccessor.ARGUMENT_ORCHESTRATOR_SERVICE)
}
