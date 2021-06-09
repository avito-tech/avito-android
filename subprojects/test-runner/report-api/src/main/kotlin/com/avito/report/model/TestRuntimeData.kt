package com.avito.report.model

/**
 * Data that can be defined only running the test
 */
public interface TestRuntimeData {

    public val incident: Incident?

    /**
     * Must be in seconds
     */
    public val startTime: Long

    /**
     * Must be in seconds
     */
    public val endTime: Long
    public val dataSetData: Map<String, String>
    public val video: Video?
    public val preconditions: List<Step>
    public val steps: List<Step>
}

public val TestRuntimeData.duration: Long
    get() = endTime - startTime
