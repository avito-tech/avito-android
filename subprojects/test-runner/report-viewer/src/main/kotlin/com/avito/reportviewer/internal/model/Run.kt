package com.avito.reportviewer.internal.model

/**
 * @see [resources/getReport.json]
 */
internal data class Run(
    val id: String,
    val planSlug: String,
    val jobSlug: String,
    val runId: String,
    val isFinished: Boolean,
    val reportData: ReportData?
)
