package com.avito.runner.finalizer

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

internal class StubFinalizerFileDumper : FinalizerFileDumper {

    override fun dump(initialTestSuite: Set<TestStaticData>, testResults: Collection<AndroidTest>) {
        // do nothing
    }
}
