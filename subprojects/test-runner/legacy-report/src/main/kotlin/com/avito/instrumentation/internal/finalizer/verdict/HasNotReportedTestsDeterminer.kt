package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData

// STOPSHIP: internal and factory
public interface HasNotReportedTestsDeterminer {

    public fun determine(
        runResult: List<SimpleRunTest>,
        allTests: List<TestStaticData>
    ): Result

    public sealed class Result {

        public open val lostTests: List<AndroidTest.Lost>
            get() = emptyList()

        public object AllTestsReported : Result()

        public data class HasNotReportedTests(
            override val lostTests: List<AndroidTest.Lost>
        ) : Result()
    }
}
