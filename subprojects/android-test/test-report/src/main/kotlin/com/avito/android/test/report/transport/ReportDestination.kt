package com.avito.android.test.report.transport

sealed class ReportDestination {

    class Backend(
        val reportApiUrl: String,
        val reportViewerUrl: String,
        val deviceName: String
    ) : ReportDestination()

    object File : ReportDestination()

    /**
     * Combination of [File] and [Backend], report.json saved to file, but screenshots and other data uploaded
     */
    object Legacy : ReportDestination()

    object NoOp : ReportDestination()
}
