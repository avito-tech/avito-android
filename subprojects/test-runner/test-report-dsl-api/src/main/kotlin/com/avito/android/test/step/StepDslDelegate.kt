package com.avito.android.test.step

import com.avito.android.test.report.model.DataSet

interface StepDslDelegate {
    fun createStep(description: String): Step
    fun createPrecondition(description: String): Step
    fun <T : DataSet> setDataSet(value: T)
}
