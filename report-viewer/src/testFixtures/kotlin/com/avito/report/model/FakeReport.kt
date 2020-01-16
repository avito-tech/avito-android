package com.avito.report.model

fun Report.Companion.createTestInstance(
    id: String = "12345",
    planSlug: String = "TestAndroid",
    jobSlug: String = "SomeTests",
    runId: String = "1234de.BuildX",
    isFinished: Boolean = false,
    buildBranch: String? = null
) = Report(id, planSlug, jobSlug, runId, isFinished, buildBranch)
