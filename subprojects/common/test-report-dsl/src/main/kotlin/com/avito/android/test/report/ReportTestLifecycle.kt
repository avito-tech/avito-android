package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepModel

interface ReportTestLifecycle<T : StepModel> : ReportStepLifecycle<T> {
    fun startTestCase()
    fun finishTestCase()
    fun failedTestCase(exception: Throwable)
    fun setDataSet(value: DataSet)
}
