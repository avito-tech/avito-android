package com.avito.android.test.report

import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata

interface InternalReport : ReportTestLifecycle<StepResult> {
    // delete init
    fun initTestCase(testMetadata: TestMetadata)

    fun unexpectedFailedTestCase(
        exception: Throwable
    )
}
