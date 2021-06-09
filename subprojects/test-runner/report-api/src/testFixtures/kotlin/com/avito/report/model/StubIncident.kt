package com.avito.report.model

public fun Incident.Companion.createStubInstance(
    type: Incident.Type = Incident.Type.ASSERTION_FAILED,
    timestamp: Long = 0,
    trace: List<String> = emptyList(),
    chain: List<IncidentElement> = emptyList(),
    entryList: List<Entry> = emptyList()
): Incident = Incident(
    type = type,
    timestamp = timestamp,
    trace = trace,
    chain = chain,
    entryList = entryList
)
