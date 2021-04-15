package com.avito.android.test.step

import com.avito.android.test.report.ReportStepModelFactory
import com.avito.android.test.report.ReportTestLifecycle
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepModel

class StepDslDelegateImpl<T : StepModel>(
    private val reportLifecycle: ReportTestLifecycle<in T>,
    private val stepModelFactory: ReportStepModelFactory<out T>
) : StepDslDelegate {

    override fun createStep(description: String): Step = StepImpl(
        stepName = description,
        report = reportLifecycle,
        stepModelFactory = stepModelFactory
    )

    override fun createPrecondition(description: String): Step {
        return Precondition(
            stepName = description,
            report = reportLifecycle,
            stepModelFactory = stepModelFactory
        )
    }

    override fun <T : DataSet> setDataSet(value: T) {
        reportLifecycle.setDataSet(value)
    }
}
