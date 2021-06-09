package com.avito.runner.config

import com.avito.runner.scheduler.suite.filter.Filter

public fun InstrumentationConfigurationData.Companion.createStubInstance(
    name: String = "name",
    instrumentationParams: InstrumentationParameters = InstrumentationParameters(),
    reportSkippedTests: Boolean = true,
    runOnlyChangedTests: Boolean = false,
    kubernetesNamespace: String = "kubernetesNamespace",
    targets: List<TargetConfigurationData> = emptyList(),
    enableDeviceDebug: Boolean = false,
    timeoutInSecond: Long = 100,
    previousRunExcluded: Set<RunStatus> = emptySet()
): InstrumentationConfigurationData = InstrumentationConfigurationData(
    name = name,
    instrumentationParams = instrumentationParams,
    reportSkippedTests = reportSkippedTests,
    runOnlyChangedTests = runOnlyChangedTests,
    kubernetesNamespace = kubernetesNamespace,
    targets = targets,
    enableDeviceDebug = enableDeviceDebug,
    timeoutInSeconds = timeoutInSecond,
    filter = InstrumentationFilterData(
        name = "stub",
        fromSource = InstrumentationFilterData.FromSource(
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
        fromRunHistory = InstrumentationFilterData.FromRunHistory(
            previousStatuses = Filter.Value(
                included = emptySet(),
                excluded = previousRunExcluded
            ),
            reportFilter = null
        )
    )
)
