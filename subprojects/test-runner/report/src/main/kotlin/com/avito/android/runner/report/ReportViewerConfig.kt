package com.avito.android.runner.report

import com.avito.report.model.ReportCoordinates
import java.io.Serializable

public data class ReportViewerConfig(
    val apiUrl: String,
    val viewerUrl: String,
    val reportCoordinates: ReportCoordinates
) : Serializable
