package com.avito.runner.scheduler.suite.filter

public fun ChangedTestsFilterInput.Companion.createStubInstance(
    mode: ChangedTestsFilterMode = ChangedTestsFilterMode.ALL,
    changedTests: List<String> = emptyList(),
    targetFilterMode: TargetFilterMode = TargetFilterMode.Uniform(mode),
): ChangedTestsFilterInput = ChangedTestsFilterInput(
    targetFilterMode = targetFilterMode,
    changedTests = changedTests
)
