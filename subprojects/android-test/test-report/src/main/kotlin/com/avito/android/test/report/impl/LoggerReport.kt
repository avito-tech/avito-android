package com.avito.android.test.report.impl

import com.avito.android.test.report.InternalReport
import com.avito.android.test.report.ReportState
import com.avito.android.test.report.StepException
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.logger.LoggerFactory
import com.avito.logger.create

internal class LoggerReport(
    loggerFactory: LoggerFactory,
    private val report: InternalReport
) : InternalReport {

    private val logger = loggerFactory.create<InternalReport>()

    override val currentState: ReportState
        get() = report.currentState

    override val isFirstStep: Boolean
        get() {
            return traceMethod("isFirstStep") {
                report.isFirstStep
            }
        }

    override fun initTestCase(testMetadata: TestMetadata) {
        traceMethod("initTestCase") {
            report.initTestCase(testMetadata)
        }
    }

    override fun unexpectedFailedTestCase(exception: Throwable) {
        traceMethod("unexpectedFailedTestCase") {
            report.unexpectedFailedTestCase(exception)
        }
    }

    override fun startTestCase() {
        traceMethod("startTestCase") {
            report.startTestCase()
        }
    }

    override fun finishTestCase() {
        traceMethod("finishTestCase") {
            report.finishTestCase()
        }
    }

    override fun failedTestCase(exception: Throwable) {
        traceMethod("failedTestCase") {
            report.failedTestCase(exception)
        }
    }

    override fun setDataSet(value: DataSet) {
        traceMethod("setDataSet") {
            report.setDataSet(value)
        }
    }

    override fun startPrecondition(step: StepResult) {
        traceMethod("startPrecondition") {
            report.startPrecondition(step)
        }
    }

    override fun stopPrecondition() {
        traceMethod("stopPrecondition") {
            report.stopPrecondition()
        }
    }

    override fun startStep(step: StepResult) {
        traceMethod("startStep") {
            report.startStep(step)
        }
    }

    override fun stopStep() {
        traceMethod("stopStep") {
            report.stopStep()
        }
    }

    override fun stepFailed(exception: StepException) {
        traceMethod("stepFailed") {
            report.stepFailed(exception)
        }
    }

    override fun preconditionFailed(exception: StepException) {
        traceMethod("preconditionFailed") {
            report.preconditionFailed(exception)
        }
    }

    override fun createStepModel(stepName: String): StepResult {
        return traceMethod("createStepModel") {
            report.createStepModel(stepName)
        }
    }

    override fun createPreconditionModel(stepName: String): StepResult {
        return traceMethod("createPreconditionModel") {
            report.createPreconditionModel(stepName)
        }
    }

    override fun addHtml(label: String, content: String, wrapHtml: Boolean) {
        traceMethod("addHtml") {
            report.addHtml(label, content, wrapHtml)
        }
    }

    override fun addText(label: String, text: String) {
        traceMethod("addText") {
            report.addText(label, text)
        }
    }

    override fun addComment(comment: String) {
        traceMethod("addComment") {
            report.addComment(comment)
        }
    }

    override fun addScreenshot(label: String) {
        traceMethod("addScreenshot") {
            report.addScreenshot(label)
        }
    }

    override fun addAssertion(label: String) {
        traceMethod("addAssertion") {
            report.addAssertion(label)
        }
    }

    private inline fun <T> traceMethod(name: String, action: () -> T): T {
        logger.debug("Method: $name execution started")
        val result = action()
        logger.debug("Method: $name execution finished")
        return result
    }
}
