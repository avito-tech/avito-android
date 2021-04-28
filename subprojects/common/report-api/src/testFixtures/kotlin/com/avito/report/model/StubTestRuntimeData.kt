package com.avito.report.model

public fun TestRuntimeDataPackage.Companion.createStubInstance(
    incident: Incident? = null,
    dataSetData: Map<String, String> = emptyMap(),
    preconditions: List<Step> = emptyList(),
    startTime: Long = 0,
    endTime: Long = 0,
    steps: List<Step> = emptyList(),
    video: Video? = null
): TestRuntimeDataPackage = TestRuntimeDataPackage(
    incident = incident,
    dataSetData = dataSetData,
    preconditions = preconditions,
    startTime = startTime,
    endTime = endTime,
    steps = steps,
    video = video
)
