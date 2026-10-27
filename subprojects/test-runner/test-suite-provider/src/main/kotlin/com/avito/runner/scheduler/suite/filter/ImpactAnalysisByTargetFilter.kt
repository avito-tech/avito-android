package com.avito.runner.scheduler.suite.filter

internal class ImpactAnalysisByTargetFilter(
    private val modeByTarget: Map<String, ImpactAnalysisMode>,
    changedTests: List<String>,
) : TestsFilter {

    override val name = "ImpactAnalysisByTarget"

    private val changedTests = changedTests.map { TestsFilter.Signatures.TestSignature(name = it) }.toSet()

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        val mode = modeByTarget[test.deviceName.name] ?: return TestsFilter.Result.Included
        val isChanged = test.matched(changedTests)
        val isIncluded = when (mode) {
            ImpactAnalysisMode.ALL -> true
            ImpactAnalysisMode.CHANGED -> isChanged
            ImpactAnalysisMode.ALL_EXCEPT_CHANGED -> !isChanged
        }
        return if (isIncluded) {
            TestsFilter.Result.Included
        } else {
            TestsFilter.Result.Excluded.NotForTarget(name, test.deviceName.name, mode)
        }
    }
}
