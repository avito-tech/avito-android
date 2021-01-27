package com.avito.instrumentation.stub.report.listener

import com.avito.instrumentation.internal.report.listener.TestReporter
import com.avito.runner.service.model.TestCase
import org.funktionale.tries.Try
import java.io.File

class StubTestReporter : TestReporter() {

    override fun report(artifacts: Try<File>, test: TestCase, executionNumber: Int) {
    }
}
