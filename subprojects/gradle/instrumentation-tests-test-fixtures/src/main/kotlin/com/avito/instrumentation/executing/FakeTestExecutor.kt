package com.avito.instrumentation.executing

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.suite.model.TestWithTarget
import java.io.File

class FakeTestExecutor : TestExecutor {

    val configurationsQueue: List<String> = mutableListOf()

    override fun execute(
        application: File,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        configuration: InstrumentationConfiguration.Data,
        runType: TestExecutor.RunType,
        output: File,
        logcatDir: File
    ) {
        (configurationsQueue as MutableList).add(configuration.name)
    }
}
