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
    prefixFilter = prefixFilter,
    tryToReRunOnTargetBranch = tryToReRunOnTargetBranch,
    rerunFailedTests = rerunFailedTests,
    reportFlakyTests = reportFlakyTests,
    reportSkippedTests = reportSkippedTests,
    annotatedWith = annotatedWith,
    impactAnalysisPolicy = impactAnalysisPolicy,
    tests = tests,
    kubernetesNamespace = kubernetesNamespace,
    targets = targets
)
