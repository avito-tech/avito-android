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

    fun addComment(comment: String)

    /**
     * Добавляет в отчет к шагу специальное поле с текстом проверяемого условия
     */
    fun addAssertion(assertionMessage: String)

    /**
     * stop request info as current test step entry
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
