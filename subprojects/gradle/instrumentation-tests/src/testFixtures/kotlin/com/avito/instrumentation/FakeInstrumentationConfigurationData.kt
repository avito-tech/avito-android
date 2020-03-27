package com.avito.instrumentation

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationParameters
import com.avito.instrumentation.configuration.target.TargetConfiguration

fun InstrumentationConfiguration.Data.Companion.createStubInstance(
    name: String = "name",
    performanceType: InstrumentationConfiguration.PerformanceType? = null,
    instrumentationParams: InstrumentationParameters = InstrumentationParameters(),
    tryToReRunOnTargetBranch: Boolean = false,
    rerunFailedTests: Boolean = false,
    reportSkippedTests: Boolean = true,
    reportFlakyTests: Boolean = false,
    prefixFilter: String? = null,
    annotatedWith: List<String>? = null,
    impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off,
    tests: List<String>? = null,
    kubernetesNamespace: String = "kubernetesNamespace",
    targets: List<TargetConfiguration.Data> = emptyList()
): InstrumentationConfiguration.Data = InstrumentationConfiguration.Data(
    name = name,
    performanceType = performanceType,
    instrumentationParams = instrumentationParams,
    keepTestsWithPrefix = prefixFilter,
    tryToReRunOnTargetBranch = tryToReRunOnTargetBranch,
    skipSucceedTestsFromPreviousRun = rerunFailedTests,
    reportFlakyTests = reportFlakyTests,
    reportSkippedTests = reportSkippedTests,
    keepTestsAnnotatedWith = annotatedWith,
    impactAnalysisPolicy = impactAnalysisPolicy,
    keepTestsWithNames = tests,
    kubernetesNamespace = kubernetesNamespace,
    targets = targets,
    keepFailedTestsFromReport = null
)
