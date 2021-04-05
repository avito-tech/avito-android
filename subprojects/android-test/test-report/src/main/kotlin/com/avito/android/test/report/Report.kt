package com.avito.android.test.report

import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage

interface Report {

    val isFirstStepOrPrecondition: Boolean

    val isWritten: Boolean

    fun initTestCase(testMetadata: TestMetadata)

    fun startTestCase()

    fun startPrecondition(step: StepResult)

    fun stopPrecondition()

    fun startStep(step: StepResult)

    fun stopStep()

    fun updateTestCase(update: ReportState.Initialized.Started.() -> Unit)

    fun reportTestCase(): ReportState.Initialized.Started

    fun makeScreenshot(comment: String): FutureValue<RemoteStorage.Result>?

    /**
     * Add comment entry with content [comment] to the current report step
     *
     * Use it with small text. Behind the scenes it inlines.
     */
    fun addComment(comment: String)

    /**
     * Add text entry with content [text] to the current report step
     *
     * @param label one-liner you see in test step comments
     *
     * Use it with big text. Behind the scenes it load text as file.
     */
    fun addText(label: String, text: String)

    /**
     * Add entry with [assertionMessage] to the current report step
     */
    fun addAssertion(assertionMessage: String)

    /**
     * Add request info as current test step entry
     *
     * @param label one-liner you see in test step comments
     * @param content detailed info about request, accessible via click on label in report
     */
    fun addHtml(label: String, content: String, wrapHtml: Boolean = true)

    fun registerIncident(
        exception: Throwable,
        screenshot: FutureValue<RemoteStorage.Result>? = null
    )
}
