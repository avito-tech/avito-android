package com.avito.android.runner

import com.avito.logger.LoggerFactory
import com.avito.logger.create

internal class ReportUncaughtHandler(
    loggerFactory: LoggerFactory,
    private val globalExceptionHandler: Thread.UncaughtExceptionHandler?,
    private val nonCriticalErrorMessages: Set<String>
) : Thread.UncaughtExceptionHandler {

    private val logger = loggerFactory.create<ReportUncaughtHandler>()

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (e.message in nonCriticalErrorMessages) {
            logger.debug("Non critical error caught by ReportUncaughtHandler. ${e.message}")
        } else {
            logger.warn("Application crashed", e)
            InHouseInstrumentationTestRunner.instance.tryToReportUnexpectedIncident(incident = e)
            globalExceptionHandler?.uncaughtException(t, e)
        }
    }
}
