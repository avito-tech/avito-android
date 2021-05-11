package com.avito.instrumentation.internal.finalizer.verdict

internal data class InstrumentationTestsTaskVerdict(
    val title: String,
    val reportUrl: String,
    val problemTests: Set<Test>
) {
    data class Test(
        val testUrl: String,
        val title: String
    )
}
