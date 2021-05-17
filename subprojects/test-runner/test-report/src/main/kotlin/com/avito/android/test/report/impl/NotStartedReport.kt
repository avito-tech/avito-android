package com.avito.android.test.report.impl

import com.avito.android.test.report.ReportState
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.transport.Transport
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider

internal class NotStartedReport(
    loggerFactory: LoggerFactory,
    transport: Transport,
    screenshotCapturer: ScreenshotCapturer,
    timeProvider: TimeProvider,
    override val state: ReportState.NotFinished.Initialized.NotStarted,
) : BaseInitializedReport(loggerFactory, transport, screenshotCapturer, timeProvider) {

    override val currentState: ReportState = state
    override val currentAttachments = state.attachmentsBeforeSteps
}
