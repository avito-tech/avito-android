package com.avito.android.test.step

import com.avito.android.test.report.ReportStepLifecycle
import com.avito.android.test.report.ReportStepModelFactory
import com.avito.android.test.report.StepException
import com.avito.android.test.report.model.StepModel

internal class StepImpl<T : StepModel>(
    stepName: String,
    report: ReportStepLifecycle<in T>,
    stepModelFactory: ReportStepModelFactory<out T>,
) : BaseStep<T>(report, stepName, stepModelFactory) {

    override val slug: String = "шагом"
    override val slug2: String = "шага"

    override fun stepStartInternal() {
        report.startStep(stepModelFactory.createStepModel(stepName))
    }

    override fun stepFailedInternal(exception: StepException) {
        report.stepFailed(exception)
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
