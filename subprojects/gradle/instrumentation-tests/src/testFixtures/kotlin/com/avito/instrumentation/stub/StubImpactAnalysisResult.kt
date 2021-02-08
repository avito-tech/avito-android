package com.avito.instrumentation

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult

fun ImpactAnalysisResult.Companion.createStubInstance(
    policy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off,
    affectedTests: List<String> = emptyList(),
    addedTests: List<String> = emptyList(),
    modifiedTests: List<String> = emptyList(),
    changedTests: List<String> = emptyList()
) = ImpactAnalysisResult(
    policy = policy,
    affectedTests = affectedTests,
    addedTests = addedTests,
    modifiedTests = modifiedTests,
    changedTests = changedTests
)
