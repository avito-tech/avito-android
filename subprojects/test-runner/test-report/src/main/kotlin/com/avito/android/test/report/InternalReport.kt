package com.avito.android.test.report

import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata

public interface InternalReport : ReportTestLifecycle<StepResult> {

    public val currentState: ReportState

    // delete init
    public fun initTestCase(testMetadata: TestMetadata)

    public fun unexpectedFailedTestCase(
        exception: Throwable
    )
}
