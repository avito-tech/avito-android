package com.avito.runner.config

import com.avito.runner.scheduler.suite.filter.Filter
import java.io.Serializable

public data class InstrumentationFilterData(
    val name: String,
    val fromSource: FromSource,
    val fromRunHistory: FromRunHistory
) : Serializable {

    public data class FromSource(
        val prefixes: Filter.Value<String>,
        val annotations: Filter.Value<String>,
        val excludeFlaky: Boolean
    ) : Serializable

    public data class FromRunHistory(
        val previousStatuses: Filter.Value<RunStatus>,
        val reportFilter: ReportFilter?
    ) : Serializable {

        public data class ReportFilter(
            val statuses: Filter.Value<RunStatus>
        ) : Serializable
    }

    public companion object
}
