package com.avito.android.test.step

import com.avito.android.test.report.ReportStepLifecycle
import com.avito.android.test.report.ReportStepModelFactory
import com.avito.android.test.report.StepException
import com.avito.android.test.report.model.StepModel

internal class Precondition<T : StepModel>(
    stepName: String,
    report: ReportStepLifecycle<in T>,
    stepModelFactory: ReportStepModelFactory<out T>,
) : BaseStep<T>(report, stepName, stepModelFactory) {

    override val slug: String = "precondition'ом"
    override val slug2: String = "precondition'a"

    override fun stepStartInternal() {
        report.startPrecondition(stepModelFactory.createPreconditionModel(stepName))
    }

    override fun stepFailedInternal(exception: StepException) {
        report.preconditionFailed(exception)
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
