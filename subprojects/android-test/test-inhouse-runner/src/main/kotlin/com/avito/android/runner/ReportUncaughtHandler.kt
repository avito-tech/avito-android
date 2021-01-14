package com.avito.android.runner

import com.avito.logger.LoggerFactory
import com.avito.logger.create

internal class ReportUncaughtHandler(
    loggerFactory: LoggerFactory,
    private val globalExceptionHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
) : Thread.UncaughtExceptionHandler {

    private val logger = loggerFactory.create<ReportUncaughtHandler>()

    override fun uncaughtException(t: Thread, e: Throwable) {
        logger.critical("Application crash captured by global handler", e)

        InHouseInstrumentationTestRunner.instance.tryToReportUnexpectedIncident(
            incident = e
        )

        globalExceptionHandler?.uncaughtException(t, e)
    }
}
