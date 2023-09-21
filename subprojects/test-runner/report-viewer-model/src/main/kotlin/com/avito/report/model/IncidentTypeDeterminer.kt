package com.avito.report.model

public interface IncidentTypeDeterminer {
    public fun determine(throwable: Throwable): Incident.Type
}
