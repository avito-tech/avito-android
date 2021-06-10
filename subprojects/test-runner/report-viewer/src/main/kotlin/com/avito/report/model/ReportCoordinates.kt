package com.avito.report.model

import java.io.Serializable

/**
 * Один уникальный запуск тест сьюта
 */
public data class ReportCoordinates(
    val planSlug: String,
    val jobSlug: String,
    val runId: String
) : Serializable {

    public companion object
}
