package com.avito.instrumentation.suite.filter

internal fun TestsFilter.Test.matched(signatures: Set<TestsFilter.Signatures.TestSignature>): Boolean {
    return signatures.any { signature ->
        name.startsWith(signature.name) && (signature.deviceName?.equals(
            deviceName.name
        ) ?: true)
    }
}

internal class IncludeByTestSignaturesFilter(
    private val source: TestsFilter.Signatures.Source,
    private val signatures: Set<TestsFilter.Signatures.TestSignature>
) : TestsFilter {
    override val name = "IncludeSignatures#$source"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return when {
            test.matched(signatures) -> TestsFilter.Result.Included
            else -> TestsFilter.Result.Excluded.DoNotMatchIncludeSignature(
                name = name,
                source = source
            )
        }
    }
}

internal class ExcludeByTestSignaturesFilter(
    private val source: TestsFilter.Signatures.Source,
    private val signatures: Set<TestsFilter.Signatures.TestSignature>
) : TestsFilter {

    override val name = "ExcludeSignatures#$source"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return when {
            test.matched(signatures) ->
                TestsFilter.Result.Excluded.MatchExcludeSignature(
                    name = name,
                    source = source
                )
            else -> TestsFilter.Result.Included
        }
    }
}
