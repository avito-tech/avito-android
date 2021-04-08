package com.avito.instrumentation.internal.verdict

internal data class InstrumentationTestsTaskVerdict(
    val title: String,
    val reportUrl: String,
    val causeFailureTests: Set<Test>
) {
    data class Test(
        val testUrl: String,
        val title: String
    )
}
