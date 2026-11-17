package com.avito.runner.scheduler.suite.filter

internal fun TestsFilter.Test.matched(signatures: Set<TestsFilter.Signatures.TestSignature>): Boolean {
    return signatures.any { signature ->
        name.startsWith(signature.name)
            && signature.deviceName?.equals(deviceName.name) ?: true
    }
}

internal data class IncludeByTestSignaturesFilter(
    private val source: TestsFilter.Signatures.Source,
    private val signatures: Set<TestsFilter.Signatures.TestSignature>
) : TestsFilter {
    override val name = "IncludeSignatures#$source"

    override fun filter(test: TestsFilter.Test): TestsFilter.Verdict {
        return when {
            test.matched(signatures) -> TestsFilter.Verdict.Included
            else -> TestsFilter.Verdict.Skipped.DoesNotMatchIncludeSignature(
                name = name,
                source = source
            )
        }
    }
}

internal data class ExcludeByTestSignaturesFilter(
    private val source: TestsFilter.Signatures.Source,
    private val signatures: Set<TestsFilter.Signatures.TestSignature>
) : TestsFilter {

    override val name = "ExcludeSignatures#$source"

    override fun filter(test: TestsFilter.Test): TestsFilter.Verdict {
        return when {
            test.matched(signatures) ->
                TestsFilter.Verdict.Skipped.MatchesExcludeSignature(
                    name = name,
                    source = source
                )
            else -> TestsFilter.Verdict.Included
        }
    }
}
