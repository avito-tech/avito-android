package com.avito.android.test.step

import com.avito.android.test.report.model.DataSet

public interface StepDslDelegate {
    public fun createStep(description: String): Step
    public fun createPrecondition(description: String): Step
    public fun <T : DataSet> setDataSet(value: T)
}
