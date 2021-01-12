package com.avito.android.runner

import android.annotation.SuppressLint
import android.util.Log

@SuppressLint("LogNotTimber")
internal class ReportUncaughtHandler(
    private val globalExceptionHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e(TAG, "Application crash captured by global handler", e)

        InHouseInstrumentationTestRunner.instance.tryToReportUnexpectedIncident(
            incident = e
        )

        globalExceptionHandler?.uncaughtException(t, e)
    }

    companion object {
        private const val TAG = "ReportUncaughtHandler"
    }
}
