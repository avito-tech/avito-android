package com.avito.android.model.output

import com.avito.reportviewer.model.ReportCoordinates

internal fun ReportCoordinates.toCdCoordinates(): CdBuildResult.TestResultsLink.ReportCoordinates {
    return CdBuildResult.TestResultsLink.ReportCoordinates(
        planSlug = planSlug,
        jobSlug = jobSlug,
        runId = runId,
    )
}
