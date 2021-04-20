package com.avito.android.test.step

import com.avito.android.test.report.ReportStepLifecycle
import com.avito.android.test.report.ReportStepModelFactory
import com.avito.android.test.report.StepException
import com.avito.android.test.report.model.StepModel

internal abstract class BaseStep<T : StepModel>(
    protected val report: ReportStepLifecycle<in T>,
    protected val stepName: String,
    protected val stepModelFactory: ReportStepModelFactory<out T>
) : Step {

    /**
     * It's completely legal to do multiple assertions in single step, but we need only one screenshot,
     * because system state must not be changed anymore in this particular step
     */
    private var screenshotBeforeAssertionDone = false

    protected abstract val slug: String
    protected abstract val slug2: String

    protected abstract fun stepStartInternal()
    protected abstract fun stepFailedInternal(exception: StepException)
    protected abstract fun stepFinishedInternal()
    protected abstract fun createStepException(assertionMessage: String?, t: Throwable): StepException

    override fun stepStart() {
        stepStartInternal()
        if (!report.isFirstStep) {
            report.addScreenshot("Screenshot перед $slug")
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
            report.addScreenshot("Screenshot перед проверкой: $assertionText")
            screenshotBeforeAssertionDone = true
        }
    }

    /**
     * For first precondition we make screenshot after step, not before, because app may not be started at this moment
     */
    override fun stepPassed() {
        if (report.isFirstStep) {
            report.addScreenshot("Screenshot после $slug2")
        }
    }

    override fun stepFailed(exception: Throwable) {
        val stepException = wrapException(exception)
        stepFailedInternal(stepException)
        throw stepException
    }

    /**
     * will be called no matter if step was successful or failed
     */
    override fun stepFinished() {
        stepFinishedInternal()
    }

    private fun wrapException(exception: Throwable): StepException {
        return exception as? StepException ?: createStepException(null, exception)
    }
}
