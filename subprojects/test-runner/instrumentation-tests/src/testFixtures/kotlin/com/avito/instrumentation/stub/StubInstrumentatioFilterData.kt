package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory
import com.avito.instrumentation.suite.filter.Filter
import java.io.Serializable

public fun <T : Serializable> emptyFilter(): Filter.Value<T> = Filter.Value<T>(
    included = emptySet(),
    excluded = emptySet()
)

public fun InstrumentationFilter.Data.Companion.createStub(
    prefixes: Filter.Value<String> = emptyFilter(),
    annotations: Filter.Value<String> = emptyFilter(),
    previousStatuses: Filter.Value<FromRunHistory.RunStatus> = emptyFilter(),
    report: InstrumentationFilter.Data.FromRunHistory.ReportFilter? = null,
    excludeFlaky: Boolean = false
): InstrumentationFilter.Data {
    return InstrumentationFilter.Data(
        name = "stub",
        fromSource = InstrumentationFilter.Data.FromSource(
            prefixes = prefixes,
            annotations = annotations,
            excludeFlaky = excludeFlaky
        ),
        fromRunHistory = InstrumentationFilter.Data.FromRunHistory(
            previousStatuses = previousStatuses,
            reportFilter = report
        )
    )
}
