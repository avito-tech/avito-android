package com.avito.runner.finalizer.verdict

public data class InstrumentationTestsTaskVerdict(
    val title: String,
    val reportUrl: String,
    val problemTests: Set<Test>
) {

    public data class Test(
        val testUrl: String,
        val title: String
    )
}
