package com.avito.android.test.report

import com.avito.android.test.report.model.StepModel

interface ReportStepModelFactory<T : StepModel> {
    fun createStepModel(stepName: String): T
    fun createPreconditionModel(stepName: String): T
}
