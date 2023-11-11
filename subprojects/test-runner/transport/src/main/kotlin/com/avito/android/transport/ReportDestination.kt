package com.avito.android.transport

import com.avito.reportviewer.model.ReportCoordinates
import okhttp3.HttpUrl

public sealed class ReportDestination {

    public data class Backend(
        public val reportApiUrl: String,
        public val reportViewerUrl: String,
        public val fileStorageUrl: HttpUrl,
        public val deviceName: String,
        public val coordinates: ReportCoordinates,
    ) : ReportDestination()

    public object File : ReportDestination()

    /**
     * Combination of [File] and [Backend], report.json saved to file, but screenshots and other data uploaded
     */
    public data class Legacy(
        public val fileStorageUrl: HttpUrl,
    ) : ReportDestination()

    public object NoOp : ReportDestination()
}
