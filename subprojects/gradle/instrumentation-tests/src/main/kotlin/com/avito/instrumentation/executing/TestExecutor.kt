package com.avito.instrumentation.executing

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.suite.model.TestWithTarget
import java.io.File

interface TestExecutor {

    fun execute(
        application: File,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        configuration: InstrumentationConfiguration.Data,
        runType: RunType,
        output: File,
        logcatDir: File
    )

    sealed class RunType {
        abstract val id: String

        data class Run(override val id: String) : RunType()
        data class Rerun(override val id: String) : RunType()
    }
}
