package com.avito.reportviewer.model

public fun ReportCoordinates.Companion.createStubInstance(
    planSlug: String = "planSlug",
    jobSlug: String = "jobSlug",
    runId: String = "runId"
): ReportCoordinates = ReportCoordinates(
    planSlug = planSlug,
    jobSlug = jobSlug,
    runId = runId
)
