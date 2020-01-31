package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState

interface Transport {

    fun send(state: ReportState.Initialized.Started)
}
