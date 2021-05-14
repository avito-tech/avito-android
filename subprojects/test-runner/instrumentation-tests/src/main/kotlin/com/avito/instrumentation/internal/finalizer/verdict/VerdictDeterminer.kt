package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

internal interface VerdictDeterminer {

    fun determine(
        initialTestSuite: Set<TestStaticData>,
        testResults: Collection<AndroidTest>
    ): Verdict
}
