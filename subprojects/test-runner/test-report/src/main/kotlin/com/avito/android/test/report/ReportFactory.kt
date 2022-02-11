package com.avito.android.test.report

import com.avito.android.test.report.impl.LoggerReport
import com.avito.android.test.report.impl.StateMachineReport
import com.avito.android.test.report.impl.SynchronizationReport
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.logger.LoggerFactory
import com.avito.report.model.IncidentTypeDeterminer
import com.avito.time.TimeProvider

public object ReportFactory {
    public fun createReport(
        loggerFactory: LoggerFactory,
        transport: Transport,
        screenshotCapturer: ScreenshotCapturer,
        timeProvider: TimeProvider,
        incidentTypeDeterminer: IncidentTypeDeterminer,
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
                    incidentTypeDeterminer = incidentTypeDeterminer,
                    troubleshooter = troubleshooter
                )
            )
        )
    }
}
