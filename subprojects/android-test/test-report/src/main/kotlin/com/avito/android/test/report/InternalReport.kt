package com.avito.android.test.report

import androidx.annotation.VisibleForTesting
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata

interface InternalReport : ReportTestLifecycle<StepResult> {

    @VisibleForTesting
    val currentState: ReportState

    // delete init
    fun initTestCase(testMetadata: TestMetadata)

    fun unexpectedFailedTestCase(
        exception: Throwable
    )
}
