package com.avito.android.test.report

import com.avito.android.test.report.model.StepModel

interface ReportStepLifecycle<T : StepModel> : ReportStepModelFactory<T>, Report {

    val isFirstStep: Boolean

    fun startPrecondition(step: T)

    fun stopPrecondition()

    fun startStep(step: T)

    fun stopStep()

    fun stepFailed(exception: StepException)

    fun preconditionFailed(exception: StepException)
}
