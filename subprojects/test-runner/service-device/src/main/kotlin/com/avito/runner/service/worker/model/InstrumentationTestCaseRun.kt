package com.avito.runner.service.worker.model

import com.avito.runner.model.TestCaseRun
import com.avito.test.model.TestName

public sealed class InstrumentationTestCaseRun {

    public data class CompletedTestCaseRun(
        val name: TestName,
        val result: TestCaseRun.Result,
        val timestampStartedMilliseconds: Long,
        val timestampCompletedMilliseconds: Long
    ) : InstrumentationTestCaseRun() {
        val durationMilliseconds: Long = timestampCompletedMilliseconds - timestampStartedMilliseconds
    }

    public data class FailedOnInstrumentationParsing(
        val message: String,
        val throwable: Throwable
    ) : InstrumentationTestCaseRun()

    public data class FailedOnStartTestCaseRun(
        val message: String
    ) : InstrumentationTestCaseRun()
}
