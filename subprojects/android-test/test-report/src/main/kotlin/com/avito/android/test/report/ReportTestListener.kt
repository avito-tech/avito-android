package com.avito.android.test.report

import android.annotation.SuppressLint
import android.util.Log
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

@SuppressLint("LogNotTimber")
class ReportTestListener : RunListener() {

    private val report: Report by lazy { TestExecutionState.reportInstance }

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
            Log.d(TAG, "Receive event: $event for test: $testName")
            action()
            Log.d(TAG, "Processed event: $event for test: $testName SUCCESSFULLY")
        } catch (t: Throwable) {
            Log.w(TAG, "Processed event: $event for test: $testName UNSUCCESSFULLY", t)
        }
    }

    companion object {
        private const val TAG = "ReportTestListener"
    }
}
