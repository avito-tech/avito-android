package com.avito.test.report.incident

import com.avito.report.model.Incident
import com.avito.report.model.IncidentTypeDeterminer

public class AssertionBasedIncidentTypeDeterminer : IncidentTypeDeterminer {
    override fun determine(throwable: Throwable): Incident.Type = when (throwable) {
        is AssertionError -> Incident.Type.ASSERTION_FAILED
        else -> throwable.cause?.let { determine(it) } ?: Incident.Type.INFRASTRUCTURE_ERROR
    }
}
