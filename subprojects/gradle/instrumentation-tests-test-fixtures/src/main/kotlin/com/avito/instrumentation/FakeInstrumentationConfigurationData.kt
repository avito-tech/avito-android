package com.avito.instrumentation

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus
import com.avito.instrumentation.configuration.InstrumentationParameters
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.suite.filter.Filter

fun InstrumentationConfiguration.Data.Companion.createStubInstance(
    name: String = "name",
    instrumentationParams: InstrumentationParameters = InstrumentationParameters(),
    reportSkippedTests: Boolean = true,
    reportFlakyTests: Boolean = false,
    impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off,
    kubernetesNamespace: String = "kubernetesNamespace",
    targets: List<TargetConfiguration.Data> = emptyList(),
    enableDeviceDebug: Boolean = false,
    timeoutInSecond: Long = 100,
    previousRunExcluded: Set<RunStatus> = emptySet()
): InstrumentationConfiguration.Data = InstrumentationConfiguration.Data(
    name = name,
    instrumentationParams = instrumentationParams,
    reportFlakyTests = reportFlakyTests,
    reportSkippedTests = reportSkippedTests,
    impactAnalysisPolicy = impactAnalysisPolicy,
    kubernetesNamespace = kubernetesNamespace,
    targets = targets,
    enableDeviceDebug = enableDeviceDebug,
    timeoutInSeconds = timeoutInSecond,
    filter = InstrumentationFilter.Data(
        name = "stub",
        fromSource = InstrumentationFilter.Data.FromSource(
            prefixes = Filter.Value(
                included = emptySet(),
                excluded = emptySet()
            ),
            annotations = Filter.Value(
                included = emptySet(),
                excluded = emptySet()
            ),
            excludeFlaky = false
        ),
        fromRunHistory = InstrumentationFilter.Data.FromRunHistory(
            previousStatuses = Filter.Value(
                included = emptySet(),
                excluded = previousRunExcluded
            ),
            reportFilter = null
        )
    )
)
