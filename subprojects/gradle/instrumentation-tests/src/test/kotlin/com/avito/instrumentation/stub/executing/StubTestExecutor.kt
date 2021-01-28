package com.avito.instrumentation.stub.executing

import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.executing.TestExecutor
import com.avito.instrumentation.internal.suite.model.TestWithTarget
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
