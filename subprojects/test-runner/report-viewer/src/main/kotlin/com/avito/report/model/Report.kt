package com.avito.report.model

/**
 * Один прогон тестов по координатам
 */
public data class Report(
    val id: String,
    val planSlug: String,
    val jobSlug: String,
    val runId: String,
    val isFinished: Boolean,
    val buildBranch: String?
) {

    public companion object
}
