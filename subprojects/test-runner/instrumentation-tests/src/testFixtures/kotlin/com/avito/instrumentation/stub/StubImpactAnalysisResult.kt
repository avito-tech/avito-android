package com.avito.instrumentation

import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult

public fun ImpactAnalysisResult.Companion.createStubInstance(
    runOnlyChangedTests: Boolean = false,
    changedTests: List<String> = emptyList()
): ImpactAnalysisResult = ImpactAnalysisResult(
    runOnlyChangedTests = runOnlyChangedTests,
    changedTests = changedTests
)
