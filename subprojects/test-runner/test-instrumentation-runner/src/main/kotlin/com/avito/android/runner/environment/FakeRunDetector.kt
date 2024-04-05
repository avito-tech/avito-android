package com.avito.android.runner.environment

import android.os.Bundle
import androidx.test.internal.runner.RunnerArgsAccessor

public object FakeRunDetector {
    public fun isRealRun(arguments: Bundle): Boolean =
        !arguments.containsKey(RunnerArgsAccessor.ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR)
}
