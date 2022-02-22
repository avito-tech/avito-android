package com.avito.android.test.report.incident

import androidx.test.espresso.EspressoException
import com.avito.report.model.Incident
import com.avito.report.model.IncidentTypeDeterminer

class EspressoBasedIncidentTypeDeterminer : IncidentTypeDeterminer {

    override fun determine(throwable: Throwable): Incident.Type = when (throwable) {
        is AssertionError, is EspressoException -> Incident.Type.ASSERTION_FAILED
        else -> throwable.cause?.let { determine(it) } ?: Incident.Type.INFRASTRUCTURE_ERROR
    }
}
