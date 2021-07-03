package com.avito.android.test.report

import com.avito.android.test.report.model.StepModel

public interface ReportStepLifecycle<T : StepModel> : ReportStepModelFactory<T>, Report {

    public val isFirstStep: Boolean

    public fun startPrecondition(step: T)

    public fun stopPrecondition()

    public fun startStep(step: T)

    public fun stopStep()

    public fun stepFailed(exception: StepException)

    public fun preconditionFailed(exception: StepException)
}
