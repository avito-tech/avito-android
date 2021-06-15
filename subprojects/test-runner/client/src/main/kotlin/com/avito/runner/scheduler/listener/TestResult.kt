package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.runner.service.model.TestCaseRun
import java.io.File

public sealed class TestResult {

    public class Complete(public val artifacts: File) : TestResult() {

        internal companion object
    }

    public class Incomplete(
        public val infraError: TestCaseRun.Result.Failed.InfrastructureError,
        public val logcat: Result<String>
    ) : TestResult() {

        internal companion object
    }

    public companion object
}
