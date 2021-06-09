package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.TestWithTarget
import java.io.File

public interface TestExecutor {

    public fun execute(
        application: File?,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        output: File
    )

    public companion object
}
