package com.avito.android.transport

public sealed class ReportDestination {

    public data class Backend(
        public val reportApiUrl: String,
        public val reportViewerUrl: String,
        public val deviceName: String
    ) : ReportDestination()

    public object File : ReportDestination()

    /**
     * Combination of [File] and [Backend], report.json saved to file, but screenshots and other data uploaded
     */
    public object Legacy : ReportDestination()

    public object NoOp : ReportDestination()
}
