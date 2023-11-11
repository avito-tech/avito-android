package com.avito.reportviewer.model

import java.io.Serializable

/**
 * Report id
 */
public data class ReportCoordinates(
    val planSlug: String,
    val jobSlug: String,
    val runId: String
) : Serializable {

    public companion object
}
