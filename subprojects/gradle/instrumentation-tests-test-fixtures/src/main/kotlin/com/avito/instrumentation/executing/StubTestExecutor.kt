package com.avito.instrumentation.executing

import com.avito.instrumentation.suite.model.TestWithTarget
import java.io.File

class StubTestExecutor : TestExecutor {

    override fun execute(
        application: File?,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        output: File
    ) {
        // empty
    }
}
