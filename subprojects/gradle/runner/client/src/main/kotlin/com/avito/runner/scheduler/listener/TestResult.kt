package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.runner.service.model.TestCaseRun
import java.io.File

sealed class TestResult {

    class Complete(val artifacts: Result<File>) : TestResult() {
        companion object
    }

    class Incomplete(val infraError: TestCaseRun.Result.Failed.InfrastructureError) : TestResult() {
        companion object
    }

    companion object
}
