package com.avito.android.test.report

import com.avito.logger.Logger
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

public abstract class AbstractReportTestListener : RunListener() {

    protected abstract val report: ReportTestLifecycle<*>

    public abstract val logger: Logger

    override fun testStarted(description: Description) {
        processEvent("start", description.displayName) {
            report.startTestCase()
        }
    }

    override fun testFinished(description: Description) {
        processEvent("finish", description.displayName) {
            report.finishTestCase()
        }
    }

    override fun testFailure(failure: Failure) {
        processEvent("failure", failure.description.displayName) {
            // we already registered it
            if (failure.exception !is StepException) {
                report.failedTestCase(failure.exception)
            }
        }
    }

    private inline fun processEvent(event: String, testName: String, action: () -> Unit) {
        try {
            logger.debug("Receive event: $event for test: $testName")
            action()
            logger.debug("Processed event: $event for test: $testName SUCCESSFULLY")
        } catch (t: Throwable) {
            logger.warn("Processed event: $event for test: $testName UNSUCCESSFULLY", t)
            throw t
        }
    }
}
