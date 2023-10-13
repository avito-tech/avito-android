package com.avito.runner.service.worker.model

import com.avito.runner.model.TestCaseRun
import com.avito.test.model.TestName

internal sealed class InstrumentationTestCaseRun {

    data class CompletedTestCaseRun(
        val name: TestName,
        val result: TestCaseRun.Result,
        val timestampStartedMilliseconds: Long,
        val timestampCompletedMilliseconds: Long
    ) : InstrumentationTestCaseRun() {
        val durationMilliseconds: Long = timestampCompletedMilliseconds - timestampStartedMilliseconds
    }

    data class FailedOnInstrumentationParsing(
        val message: String,
        val throwable: Throwable
    ) : InstrumentationTestCaseRun()

    data class FailedOnStartTestCaseRun(
        val message: String
    ) : InstrumentationTestCaseRun()
}
