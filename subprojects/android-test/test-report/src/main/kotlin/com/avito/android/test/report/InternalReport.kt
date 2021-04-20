package com.avito.android.test.report

import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata

interface InternalReport : ReportTestLifecycle<StepResult> {

    val isWritten: Boolean

    // delete init
    fun initTestCase(testMetadata: TestMetadata)

    fun registerIncident(
        exception: Throwable
    )

    fun registerIncident(
        exception: Throwable,
        screenshotName: String
    )
}
