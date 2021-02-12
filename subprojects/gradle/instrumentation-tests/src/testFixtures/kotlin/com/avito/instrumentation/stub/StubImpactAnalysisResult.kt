package com.avito.instrumentation

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult

public fun ImpactAnalysisResult.Companion.createStubInstance(
    policy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off,
    affectedTests: List<String> = emptyList(),
    addedTests: List<String> = emptyList(),
    modifiedTests: List<String> = emptyList(),
    changedTests: List<String> = emptyList()
): ImpactAnalysisResult = ImpactAnalysisResult(
    policy = policy,
    affectedTests = affectedTests,
    addedTests = addedTests,
    modifiedTests = modifiedTests,
    changedTests = changedTests
)
