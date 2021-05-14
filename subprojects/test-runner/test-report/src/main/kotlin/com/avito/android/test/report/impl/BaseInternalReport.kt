package com.avito.android.test.report.impl

import com.avito.android.test.report.InternalReport
import com.avito.android.test.report.StepException
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata

internal abstract class BaseInternalReport : InternalReport {

    override val isFirstStep: Boolean
        get() = throw UnsupportedOperationException("$this do not support this")

    override fun initTestCase(testMetadata: TestMetadata) {
        throw UnsupportedOperationException("$this does not support initTestCase")
    }

    override fun unexpectedFailedTestCase(exception: Throwable) {
        throw UnsupportedOperationException("$this does not support unexpectedFailedTestCase")
    }

    override fun startTestCase() {
        throw UnsupportedOperationException("$this does not support startTestCase")
    }

    override fun finishTestCase() {
        throw UnsupportedOperationException("$this does not support finishTestCase")
    }

    override fun failedTestCase(exception: Throwable) {
        throw UnsupportedOperationException("$this does not support failedTestCase")
    }

    override fun setDataSet(value: DataSet) {
        throw UnsupportedOperationException("$this does not support setDataSet")
    }

    override fun startPrecondition(step: StepResult) {
        throw UnsupportedOperationException("$this does not support startPrecondition")
    }

    override fun stopPrecondition() {
        throw UnsupportedOperationException("$this does not support stopPrecondition")
    }

    override fun startStep(step: StepResult) {
        throw UnsupportedOperationException("$this does not support startStep")
    }

    override fun stopStep() {
        throw UnsupportedOperationException("$this does not support stopStep")
    }

    override fun stepFailed(exception: StepException) {
        throw UnsupportedOperationException("$this does not support stepFailed")
    }

    override fun preconditionFailed(exception: StepException) {
        throw UnsupportedOperationException("$this does not support preconditionFailed")
    }

    override fun createStepModel(stepName: String): StepResult {
        throw UnsupportedOperationException("$this does not support createStepModel")
    }

    override fun createPreconditionModel(stepName: String): StepResult {
        throw UnsupportedOperationException("$this does not support createPreconditionModel")
    }

    override fun addHtml(label: String, content: String, wrapHtml: Boolean) {
        throw UnsupportedOperationException("$this does not support addHtml")
    }

    override fun addText(label: String, text: String) {
        throw UnsupportedOperationException("$this does not support addText")
    }

    override fun addComment(comment: String) {
        throw UnsupportedOperationException("$this does not support addComment")
    }

    override fun addScreenshot(label: String) {
        throw UnsupportedOperationException("$this does not support addScreenshot")
    }

    override fun addAssertion(label: String) {
        throw UnsupportedOperationException("$this does not support addAssertion")
    }
}
