package com.avito.android.test.report

import com.avito.android.elastic.ElasticConfig
import com.avito.android.log.AndroidLoggerFactory
import com.avito.android.sentry.SentryConfig
import com.avito.logger.create
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

class ReportTestListener : RunListener() {

    private val report: Report by lazy { TestExecutionState.reportInstance }
    private val logger = AndroidLoggerFactory(
        elasticConfig = ElasticConfig.Disabled,
        sentryConfig = SentryConfig.Disabled
    ).create<ReportTestListener>()

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
