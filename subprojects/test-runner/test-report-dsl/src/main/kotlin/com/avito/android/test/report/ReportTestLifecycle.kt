package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepModel

public interface ReportTestLifecycle<T : StepModel> : ReportStepLifecycle<T> {
    public fun startTestCase()
    public fun finishTestCase()
    public fun failedTestCase(exception: Throwable)
    public fun setDataSet(value: DataSet)
}
