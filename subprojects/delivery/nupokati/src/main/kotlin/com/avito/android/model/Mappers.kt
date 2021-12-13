package com.avito.android.model

import com.avito.reportviewer.model.ReportCoordinates

internal fun ReportCoordinates.toCdCoordinates(): CdBuildResult.TestResultsLink.ReportCoordinates {
    return CdBuildResult.TestResultsLink.ReportCoordinates(
        planSlug = planSlug,
        jobSlug = jobSlug,
        runId = runId,
    )
}
