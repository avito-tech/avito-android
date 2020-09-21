package com.avito.instrumentation

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.suite.filter.ImpactAnalysisResult

fun ImpactAnalysisResult.Companion.createStubInstance(
    impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off,
    affectedTests: List<String> = emptyList(),
    addedTests: List<String> = emptyList(),
    modifiedTests: List<String> = emptyList()
) = ImpactAnalysisResult(
    policy = impactAnalysisPolicy,
    affectedTests = affectedTests,
    addedTests = addedTests,
    modifiedTests = modifiedTests
)
