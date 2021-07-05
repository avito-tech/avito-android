package com.avito.ci.internal

import com.avito.reportviewer.model.ReportCoordinates
import java.util.Locale

/**
 * RunId doesn't matter here, because it's stable for a single build
 */
internal data class ReportKey(val planSlug: String, val jobSlug: String) {

    fun appendToTaskName(taskName: String): String {
        return "$taskName${planSlug.capitalize(Locale.getDefault())}${jobSlug.capitalize(Locale.getDefault())}"
    }

    internal companion object {

        internal fun fromReportCoordinates(reportCoordinates: ReportCoordinates): ReportKey {
            return ReportKey(planSlug = reportCoordinates.planSlug, jobSlug = reportCoordinates.jobSlug)
        }
    }
}
