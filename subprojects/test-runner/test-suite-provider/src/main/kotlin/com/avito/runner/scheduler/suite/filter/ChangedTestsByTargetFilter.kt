package com.avito.runner.scheduler.suite.filter

internal class ChangedTestsByTargetFilter(
    private val modesByTarget: Map<String, ChangedTestsFilterMode>,
    changedTests: List<String>,
) : TestsFilter {

    override val name = "ChangedTestsByTarget"

    private val changedTests = changedTests.map { TestsFilter.Signatures.TestSignature(name = it) }.toSet()

    override fun filter(test: TestsFilter.Test): TestsFilter.Verdict {
        val mode = requireNotNull(modesByTarget[test.deviceName.name]) {
            "No changed tests filter mode for target '${test.deviceName.name}'"
        }
        val isChanged = test.matched(changedTests)
        val isIncluded = when (mode) {
            ChangedTestsFilterMode.ALL -> true
            ChangedTestsFilterMode.CHANGED -> isChanged
            ChangedTestsFilterMode.ALL_EXCEPT_CHANGED -> !isChanged
        }
        return if (isIncluded) {
            TestsFilter.Verdict.Included
        } else {
            TestsFilter.Verdict.NotForTarget(name, test.deviceName.name, mode)
        }
    }
}
