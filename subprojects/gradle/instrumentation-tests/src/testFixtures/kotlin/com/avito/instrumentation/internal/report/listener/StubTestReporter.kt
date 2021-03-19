package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.runner.service.model.TestCase
import java.io.File

internal class StubTestReporter : TestReporter() {

    override fun report(artifacts: Result<File>, test: TestCase, executionNumber: Int) {
    }
}
