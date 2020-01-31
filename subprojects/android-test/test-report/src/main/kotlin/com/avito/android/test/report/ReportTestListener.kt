package com.avito.android.test.report

import android.annotation.SuppressLint
import android.util.Log
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

@SuppressLint("LogNotTimber")
class ReportTestListener : RunListener() {

    private val report: Report by lazy { reportInstance }

    override fun testStarted(description: Description) {
        Log.d(TAG, "Receive testStarted event for ${description.displayName}")

        try {
            report.startTestCase()
        } catch (t: Throwable) {
            Log.e(
                TAG,
                "Failed to process testStarted event for ${description.displayName} with error: ${t.message}"
            )
            report.sentry.sendException(t)
        }

        Log.d(TAG, "TestStarted event processing completed for ${description.displayName}")
    }

    override fun testFinished(description: Description) {
        Log.d(TAG, "Receive testFinished event for ${description.displayName}")

        try {
            report.reportTestCase()
        } catch (t: Throwable) {
            Log.e(
                TAG,
                "Failed to send testFinished event for ${description.displayName} with error: ${t.message}"
            )
            report.sentry.sendException(t)
        }
        Log.d(TAG, "TestFinished event processing completed for ${description.displayName}")
    }

    override fun testFailure(failure: Failure) {
        Log.d(TAG, "Receive testFailure event for ${failure.description.displayName}")

        try {
            // we already registered it
            if (failure.exception !is StepException) {
                report.registerIncident(
                    exception = failure.exception,
                    screenshot = report.makeScreenshot("testFailure in listener")
                )
            }
        } catch (t: Throwable) {
            Log.e(
                TAG,
                "Failed to process testFailure event for ${failure.description.displayName} with error: ${t.message}"
            )
            report.sentry.sendException(t)
        }

        Log.d(
            TAG,
            "TestFailure event processing completed for ${failure.description.displayName}"
        )
    }

    companion object {
        private const val TAG = "ReportTestListener"
    }
}
