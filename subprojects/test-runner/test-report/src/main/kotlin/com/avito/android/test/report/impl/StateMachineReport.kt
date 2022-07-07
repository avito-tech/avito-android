package com.avito.android.test.report.impl

import com.avito.android.test.report.InternalReport
import com.avito.android.test.report.ReportState
import com.avito.android.test.report.StepException
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.logger.LoggerFactory
import com.avito.report.model.IncidentTypeDeterminer
import com.avito.time.TimeProvider

internal class StateMachineReport(
    private val loggerFactory: LoggerFactory,
    private val transport: Transport,
    private val screenshotCapturer: ScreenshotCapturer,
    private val timeProvider: TimeProvider,
    private val troubleshooter: Troubleshooter,
    private val incidentTypeDeterminer: IncidentTypeDeterminer
) : InternalReport {

    @Volatile
    private var currentReport: InternalReport = NotInitializedReport(ReportState.NotFinished.NotInitialized)

    override val currentState: ReportState
        get() = currentReport.currentState

    override val isFirstStep: Boolean
        get() = currentReport.isFirstStep

    override fun initTestCase(testMetadata: TestMetadata) {
        checkState<NotInitializedReport>()
        currentReport = NotStartedReport(
            loggerFactory = loggerFactory,
            transport = transport,
            screenshotCapturer = screenshotCapturer,
            timeProvider = timeProvider,
            state = ReportState.NotFinished.Initialized.NotStarted(
                testMetadata = testMetadata
            )
        )
    }

    override fun startTestCase() {
        val report = checkState<NotStartedReport>()
        currentReport = StartedReport(
            loggerFactory = loggerFactory,
            transport = transport,
            screenshotCapturer = screenshotCapturer,
            timeProvider = timeProvider,
            incidentTypeDeterminer = incidentTypeDeterminer,
            troubleshooter = troubleshooter,
            state = ReportState.NotFinished.Initialized.Started(
                attachmentsBeforeSteps = report.state.attachmentsBeforeSteps,
                testMetadata = report.state.testMetadata,
                startTime = timeProvider.nowInSeconds()
            )
        )
        currentReport.startTestCase()
    }

    override fun finishTestCase() {
        checkState<StartedReport>()
        currentReport.finishTestCase()
        currentReport = FinishedReport(loggerFactory)
    }

    private inline fun <reified T : InternalReport> checkState(): T {
        val report = currentReport
        check(report is T) {
            "Incorrect report state $currentReport. Must be ${T::class.java}"
        }
        return report
    }

    override fun unexpectedFailedTestCase(exception: Throwable) {
        currentReport.unexpectedFailedTestCase(exception)
    }

    override fun failedTestCase(exception: Throwable) {
        currentReport.failedTestCase(exception)
    }

    override fun setDataSet(value: DataSet) {
        currentReport.setDataSet(value)
    }

    override fun startPrecondition(step: StepResult) {
        currentReport.startPrecondition(step)
    }

    override fun stopPrecondition() {
        currentReport.stopPrecondition()
    }

    override fun startStep(step: StepResult) {
        currentReport.startStep(step)
    }

    override fun stopStep() {
        currentReport.stopStep()
    }

    override fun stepFailed(exception: StepException) {
        currentReport.stepFailed(exception)
    }

    override fun preconditionFailed(exception: StepException) {
        currentReport.preconditionFailed(exception)
    }

    override fun createStepModel(stepName: String): StepResult {
        return currentReport.createStepModel(stepName)
    }

    override fun createPreconditionModel(stepName: String): StepResult {
        return currentReport.createPreconditionModel(stepName)
    }

    override fun addHtml(label: String, content: String, wrapHtml: Boolean) {
        currentReport.addHtml(label, content, wrapHtml)
    }

    override fun addText(label: String, text: String) {
        currentReport.addText(label, text)
    }

    override fun addComment(comment: String) {
        currentReport.addComment(comment)
    }

    override fun addScreenshot(label: String) {
        currentReport.addScreenshot(label)
    }

    override fun addAssertion(label: String) {
        currentReport.addAssertion(label)
    }
}
