package com.avito.instrumentation.executing

import com.avito.instrumentation.suite.model.TestWithTarget
import java.io.File

interface TestExecutor {

    fun execute(
        application: File?,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        output: File
    )

    data class RunType(val id: String)
}
