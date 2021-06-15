package com.avito.instrumentation.configuration

import java.io.Serializable

public data class ReportViewer(
    val reportApiUrl: String,
    val reportViewerUrl: String,
    val reportRunIdPrefix: String,
    val fileStorageUrl: String
) : Serializable
