package com.avito.runner.scheduler.suite.filter

public fun ImpactAnalysisResult.Companion.createStubInstance(
    runOnlyChangedTests: Boolean = false,
    changedTests: List<String> = emptyList()
): ImpactAnalysisResult = ImpactAnalysisResult(
    runOnlyChangedTests = runOnlyChangedTests,
    changedTests = changedTests
)
