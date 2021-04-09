package com.avito.android.test.report

import com.avito.android.test.report.model.StepResult

@PublishedApi
internal abstract class BaseStep(
    protected val report: Report,
    protected val stepName: String
) : TestCaseAssertion {

    /**
     * It's completely legal to do multiple assertions in single step, but we need only one screenshot,
     * because system state must not be changed anymore in this particular step
     */
    private var screenshotBeforeAssertionDone = false

    protected abstract val slug: String
    protected abstract val slug2: String

    protected abstract fun stepStartInternal(stepResult: StepResult)
    protected abstract fun stepFinishedInternal()
    protected abstract fun createStepException(assertionMessage: String?, t: Throwable): StepException

    fun stepStart() {
        val newStep = StepResult(isSynthetic = false).apply { title = stepName }
        stepStartInternal(newStep)
        if (!report.isFirstStepOrPrecondition) {
            report.makeScreenshot("Screenshot перед $slug")
        }
    }

    override fun assertion(assertionMessage: String, action: () -> Unit) {
        try {
            beforeAssertion(assertionMessage)
            action()
        } catch (t: Throwable) {
            throw createStepException(assertionMessage, t)
        }
    }

    /**
     * We need screenshot in the moment right before first assertion.
     * Assume that we don't change system state anymore for this step
     */
    private fun beforeAssertion(assertionText: String) {
        report.addAssertion(assertionText)

        if (!screenshotBeforeAssertionDone) {
            report.makeScreenshot("Screenshot перед проверкой: $assertionText")
            screenshotBeforeAssertionDone = true
        }
    }

    /**
     * For first precondition we make screenshot after step, not before, because app may not be started at this moment
     */
    fun stepPassed() {
        if (report.isFirstStepOrPrecondition) {
            report.makeScreenshot("Screenshot после $slug2")
        }
    }

    fun stepFailed(exception: Throwable) {
        val stepException = wrapException(exception)
        report.registerIncident(
            stepException,
            report.makeScreenshot("Screenshot после падения $slug2")
        )
        throw stepException
    }

    /**
     * will be called no matter if step was successful or failed
     */
    fun stepFinished() {
        stepFinishedInternal()
    }

    private fun wrapException(exception: Throwable): StepException {
        return exception as? StepException ?: createStepException(null, exception)
    }
}

@PublishedApi
internal class Step(
    stepName: String,
    report: Report
) : BaseStep(report, stepName) {

    override val slug: String = "шагом"
    override val slug2: String = "шага"

    override fun stepStartInternal(stepResult: StepResult) {
        report.startStep(stepResult)
    }

    override fun stepFinishedInternal() {
        report.stopStep()
    }

    override fun createStepException(
        assertionMessage: String?,
        t: Throwable
    ): StepException =
        StepException(false, stepName, assertionMessage, t)
}

@PublishedApi
internal class Precondition(
    stepName: String,
    report: Report
) : BaseStep(report, stepName) {

    override val slug: String = "precondition'ом"
    override val slug2: String = "precondition'a"

    override fun stepStartInternal(stepResult: StepResult) {
        report.startPrecondition(stepResult)
    }

    override fun stepFinishedInternal() {
        report.stopPrecondition()
    }

    override fun createStepException(
        assertionMessage: String?,
        t: Throwable
    ): StepException =
        StepException(true, stepName, assertionMessage, t)
}
