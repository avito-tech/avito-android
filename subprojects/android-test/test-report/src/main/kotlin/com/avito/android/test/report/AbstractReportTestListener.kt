package com.avito.android.test.report

import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

abstract class AbstractReportTestListener : RunListener() {

    private val report: Report by lazy { TestExecutionState.reportInstance }

    private val logger: Logger by lazy { loggerFactory.create<AbstractReportTestListener>() }

    protected abstract val loggerFactory: LoggerFactory

    override fun testStarted(description: Description) {
        processEvent("start", description.displayName) {
            report.startTestCase()
        }
    }

    override fun testFinished(description: Description) {
        processEvent("finish", description.displayName) {
            report.reportTestCase()
        }
    }

    override fun testFailure(failure: Failure) {
        processEvent("failure", failure.description.displayName) {
            // we already registered it
            if (failure.exception !is StepException) {
                report.registerIncident(
                    exception = failure.exception,
                    screenshot = report.makeScreenshot("testFailure in listener")
                )
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
        }
    }
}
