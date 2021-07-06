package com.avito.reportviewer.model

/**
 * Один прогон тестов по координатам
 *
 * todo internal
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
