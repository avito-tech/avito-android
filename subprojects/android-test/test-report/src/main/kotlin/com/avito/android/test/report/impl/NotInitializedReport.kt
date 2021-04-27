package com.avito.android.test.report.impl

import com.avito.android.test.report.ReportState

internal class NotInitializedReport(
    internal val state: ReportState.NotFinished.NotInitialized
) : BaseInternalReport() {

    override val currentState: ReportState = state
}
