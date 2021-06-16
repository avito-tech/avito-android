package com.avito.runner.finalizer

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

internal interface FinalizerFileDumper {

    fun dump(
        initialTestSuite: Set<TestStaticData>,
        testResults: Collection<AndroidTest>
    )
}
