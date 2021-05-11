package com.avito.report.model

public data class TestRuntimeDataPackage(
    override val incident: Incident?,
    override val startTime: Long,
    override val endTime: Long,
    override val dataSetData: Map<String, String>,
    override val video: Video?,
    override val preconditions: List<Step>,
    override val steps: List<Step>
) : TestRuntimeData {

    // for test fixtures
    public companion object
}
