package com.avito.instrumentation

import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult

public fun ImpactAnalysisResult.Companion.createStubInstance(
    runOnlyChangedTests: Boolean = false,
    affectedTests: List<String> = emptyList(),
    addedTests: List<String> = emptyList(),
    modifiedTests: List<String> = emptyList(),
    changedTests: List<String> = emptyList()
): ImpactAnalysisResult = ImpactAnalysisResult(
    runOnlyChangedTests = runOnlyChangedTests,
    affectedTests = affectedTests,
    addedTests = addedTests,
    modifiedTests = modifiedTests,
    changedTests = changedTests
)
