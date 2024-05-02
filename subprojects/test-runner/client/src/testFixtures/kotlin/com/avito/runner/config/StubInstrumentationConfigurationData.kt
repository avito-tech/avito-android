package com.avito.runner.config

import com.avito.runner.scheduler.suite.config.InstrumentationFilterData
import com.avito.runner.scheduler.suite.config.RunStatus
import com.avito.runner.scheduler.suite.filter.Filter
import java.time.Duration

public fun InstrumentationConfigurationData.Companion.createStubInstance(
    name: String = "name",
    instrumentationParams: InstrumentationParameters = InstrumentationParameters(),
    reportSkippedTests: Boolean = true,
    targets: List<TargetConfigurationData> = emptyList(),
    testRunnerExecutionTimeout: Duration = Duration.ofSeconds(100),
    instrumentationTaskTimeout: Duration = Duration.ofSeconds(120),
    singleTestRunTimeout: Duration = Duration.ofMinutes(5L),
    previousRunExcluded: Set<RunStatus> = emptySet(),
): InstrumentationConfigurationData = InstrumentationConfigurationData(
    name = name,
    instrumentationParams = instrumentationParams,
    reportSkippedTests = reportSkippedTests,
    targets = targets,
    testRunnerExecutionTimeout = testRunnerExecutionTimeout,
    instrumentationTaskTimeout = instrumentationTaskTimeout,
    singleTestRunTimeout = singleTestRunTimeout,
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
    ),
    reportConfig = RunnerReportConfig.None,
)
