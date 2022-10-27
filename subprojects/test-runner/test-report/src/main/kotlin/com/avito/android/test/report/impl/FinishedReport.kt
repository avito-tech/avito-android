package com.avito.android.test.report.impl

import com.avito.android.test.report.ReportState
import com.avito.logger.LoggerFactory
import com.avito.logger.create

internal class FinishedReport(
    loggerFactory: LoggerFactory,
) : BaseInternalReport() {

    private val logger = loggerFactory.create<FinishedReport>()

    override val currentState: ReportState
        get() = ReportState.Finished

    override fun unexpectedFailedTestCase(exception: Throwable) {
        logger.warn("Fail to register unexpected incident. Report is already written", exception)
    }

    override fun addHtml(label: String, content: String, wrapHtml: Boolean) {
        logger.warn("Fail to addHtml $label")
    }

    override fun addText(label: String, text: String) {
        logger.warn("Fail to addText $label")
    }

    override fun addComment(comment: String) {
        logger.warn("Fail to addComment $comment")
    }

    override fun addScreenshot(label: String) {
        logger.warn("Fail to addScreenshot $label")
    }

    override fun addAssertion(label: String) {
        logger.warn("Fail to addAssertion $label")
    }
}
