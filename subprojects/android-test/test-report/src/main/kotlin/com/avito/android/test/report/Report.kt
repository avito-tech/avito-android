package com.avito.android.test.report

import androidx.test.espresso.EspressoException
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import io.sentry.SentryClient
import okhttp3.HttpUrl

interface Report {

    val isFirstStepOrPrecondition: Boolean
    @Deprecated("")
    val sentry: SentryClient

    fun initTestCase(testMetadata: TestMetadata)

    fun startTestCase()

    fun startPrecondition(step: StepResult)

    fun stopPrecondition()

    fun startStep(step: StepResult)

    fun updateStep(update: StepResult.() -> Unit)

    fun stopStep()

    fun updateTestCase(update: ReportState.Initialized.Started.() -> Unit)

    fun reportTestCase(): ReportState.Initialized.Started

    fun makeScreenshot(comment: String): FutureValue<RemoteStorage.Result>?

    fun addComment(comment: String)

    fun addEntry(entry: Entry)

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
    fun addHtml(label: String, content: String)

    fun registerIncident(exception: Throwable)

    fun registerIncident(
        exception: Throwable,
        screenshot: FutureValue<RemoteStorage.Result>?,
        type: Incident.Type = exception.determineIncidentType()
    )
}

internal fun Throwable?.determineIncidentType(): Incident.Type {
    return when {
        this == null -> Incident.Type.INFRASTRUCTURE_ERROR
        else -> when (this) {
            is AssertionError, is EspressoException -> Incident.Type.ASSERTION_FAILED
            else -> when {
                this.cause != null -> this.cause.determineIncidentType()
                else -> Incident.Type.INFRASTRUCTURE_ERROR
            }
        }
    }
}
