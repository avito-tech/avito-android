package com.avito.instrumentation.internal.report.listener

import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.model.TestCase

internal class StubTestReporter : TestReporter() {

    override fun report(result: TestLifecycleListener.TestResult, test: TestCase, executionNumber: Int) {
        // no op
    }
}
