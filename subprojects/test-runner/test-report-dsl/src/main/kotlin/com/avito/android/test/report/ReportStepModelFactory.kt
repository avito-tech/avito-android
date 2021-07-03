package com.avito.android.test.report

import com.avito.android.test.report.model.StepModel

public interface ReportStepModelFactory<T : StepModel> {
    public fun createStepModel(stepName: String): T
    public fun createPreconditionModel(stepName: String): T
}
