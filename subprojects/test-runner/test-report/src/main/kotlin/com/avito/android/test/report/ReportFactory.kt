package com.avito.android.test.report

import com.avito.android.test.report.impl.LoggerReport
import com.avito.android.test.report.impl.StateMachineReport
import com.avito.android.test.report.impl.SynchronizationReport
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider

object ReportFactory {
    fun createReport(
        loggerFactory: LoggerFactory,
        transport: Transport,
        screenshotCapturer: ScreenshotCapturer,
        timeProvider: TimeProvider,
        troubleshooter: Troubleshooter
    ): InternalReport {
        return LoggerReport(
            loggerFactory = loggerFactory,
            report = SynchronizationReport(
                report = StateMachineReport(
                    loggerFactory = loggerFactory,
                    transport = transport,
                    screenshotCapturer = screenshotCapturer,
                    timeProvider = timeProvider,
                    troubleshooter = troubleshooter
                )
            )
        )
    }
}
