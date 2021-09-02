package com.avito.android.runner

import android.os.Bundle
import androidx.test.internal.runner.RunnerArgsAccessor

interface OrchestratorDelegate {

    fun isRealRun(arguments: Bundle): Boolean =
        !arguments.containsKey(RunnerArgsAccessor.ARGUMENT_ORCHESTRATOR_SERVICE)
}
