package com.avito.runner.scheduler.suite.filter

public fun ImpactAnalysisResult.Companion.createStubInstance(
    mode: ImpactAnalysisMode = ImpactAnalysisMode.ALL,
    changedTests: List<String> = emptyList(),
    selection: ImpactAnalysisSelection = ImpactAnalysisSelection.Uniform(mode),
): ImpactAnalysisResult = ImpactAnalysisResult(
    selection = selection,
    changedTests = changedTests
)
