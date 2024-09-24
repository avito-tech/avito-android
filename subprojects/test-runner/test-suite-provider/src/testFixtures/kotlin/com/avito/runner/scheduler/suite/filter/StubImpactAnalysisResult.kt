package com.avito.runner.scheduler.suite.filter

public fun ImpactAnalysisResult.Companion.createStubInstance(
    mode: ImpactAnalysisMode = ImpactAnalysisMode.ALL,
    changedTests: List<String> = emptyList()
): ImpactAnalysisResult = ImpactAnalysisResult(
    mode = mode,
    changedTests = changedTests
)
