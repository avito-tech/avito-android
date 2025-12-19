package com.avito.android.model.output

import com.avito.android.http.nupokati.retrofit.model.save_test_result.SaveTestResultRequest
import com.avito.reportviewer.model.ReportCoordinates

internal fun ReportCoordinates.toCdCoordinates(): CdBuildResult.TestResultsLink.ReportCoordinates {
    return CdBuildResult.TestResultsLink.ReportCoordinates(
        planSlug = planSlug,
        jobSlug = jobSlug,
        runId = runId,
    )
}

internal fun ReportCoordinates.toNupokatiV4ReportCoordinates(): SaveTestResultRequest.ReportCoordinates {
    return SaveTestResultRequest.ReportCoordinates(
        planSlug = planSlug,
        jobSlug = jobSlug,
        runId = runId,
    )
}
