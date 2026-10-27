package com.avito.runner.scheduler.suite.config

import com.avito.runner.scheduler.suite.filter.Filter
import com.avito.runner.scheduler.suite.filter.ImpactAnalysisMode
import java.io.Serializable

public data class InstrumentationFilterData(
    val name: String,
    val fromSource: FromSource,
    val fromRunHistory: FromRunHistory,
    val fromImpactAnalysis: FromImpactAnalysis = FromImpactAnalysis(modeByTarget = emptyMap()),
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
            val reportId: String,
            val statuses: Filter.Value<RunStatus>
        ) : Serializable
    }

    /**
     * Splits tests between targets of one run: target name -> which tests of the change it runs.
     */
    public data class FromImpactAnalysis(
        val modeByTarget: Map<String, ImpactAnalysisMode>
    ) : Serializable

    public companion object
}
