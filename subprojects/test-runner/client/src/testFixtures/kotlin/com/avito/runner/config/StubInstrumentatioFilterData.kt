package com.avito.runner.config

import com.avito.runner.scheduler.suite.filter.Filter
import java.io.Serializable

public fun <T : Serializable> emptyFilter(): Filter.Value<T> = Filter.Value<T>(
    included = emptySet(),
    excluded = emptySet()
)

public fun InstrumentationFilterData.Companion.createStub(
    prefixes: Filter.Value<String> = emptyFilter(),
    annotations: Filter.Value<String> = emptyFilter(),
    previousStatuses: Filter.Value<RunStatus> = emptyFilter(),
    report: InstrumentationFilterData.FromRunHistory.ReportFilter? = null,
    excludeFlaky: Boolean = false
): InstrumentationFilterData {
    return InstrumentationFilterData(
        name = "stub",
        fromSource = InstrumentationFilterData.FromSource(
            prefixes = prefixes,
            annotations = annotations,
            excludeFlaky = excludeFlaky
        ),
        fromRunHistory = InstrumentationFilterData.FromRunHistory(
            previousStatuses = previousStatuses,
            reportFilter = report
        )
    )
}
