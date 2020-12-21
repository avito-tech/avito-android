package com.avito.report.model

fun ReportCoordinates.Companion.createStubInstance(
    planSlug: String = "planSlug",
    jobSlug: String = "jobSlug",
    runId: String = "runId"
): ReportCoordinates = ReportCoordinates(
    planSlug = planSlug,
    jobSlug = jobSlug,
    runId = runId
)
