package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory
import com.avito.instrumentation.suite.filter.Filter
import java.io.Serializable

fun <T : Serializable> emptyFilter() = Filter.Value<T>(
    included = emptySet(),
    excluded = emptySet()
)

fun InstrumentationFilter.Data.Companion.createStub(
    prefixes: Filter.Value<String> = emptyFilter(),
    annotations: Filter.Value<String> = emptyFilter(),
    previousStatuses: Filter.Value<FromRunHistory.RunStatus> = emptyFilter()
): InstrumentationFilter.Data {
    return InstrumentationFilter.Data(
        name = "stub",
        fromSource = InstrumentationFilter.Data.FromSource(
            prefixes = prefixes,
            annotations = annotations
        ),
        fromRunHistory = InstrumentationFilter.Data.FromRunHistory(
            previousStatuses = previousStatuses,
            reportFilter = null
        )
    )
}