package com.avito.instrumentation.report.listener

import com.avito.runner.service.model.TestCase
import org.funktionale.tries.Try
import java.io.File

class FakeTestReporter : TestReporter() {

    override fun report(artifacts: Try<File>, test: TestCase, executionNumber: Int) {
    }
}
