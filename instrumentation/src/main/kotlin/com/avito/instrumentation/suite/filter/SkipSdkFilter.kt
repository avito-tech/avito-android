package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName

class SkipSdkFilter : TestRunFilter {

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict {

        //todo annotation filter parent/delegate
        val testAnnotations = test.annotations
            .find { it.name == "com.avito.android.test.annotations.SkipOnSdk" }

        @Suppress("UNCHECKED_CAST")
        val skippedSdks = testAnnotations?.values?.get("sdk") as? Collection<Int>

        return if (skippedSdks?.contains(api) == true) {
            TestRunFilter.Verdict.Skip.SkippedBySdk
        } else {
            TestRunFilter.Verdict.Run
        }
    }
}
