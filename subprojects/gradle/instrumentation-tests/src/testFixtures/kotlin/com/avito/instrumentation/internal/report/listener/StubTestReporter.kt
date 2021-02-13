package com.avito.instrumentation.internal.report.listener

import com.avito.runner.service.model.TestCase
import org.funktionale.tries.Try
import java.io.File

internal class StubTestReporter : TestReporter() {

    override fun report(artifacts: Try<File>, test: TestCase, executionNumber: Int) {
    }
}
