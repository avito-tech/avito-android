package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName

class AnnotatedWithFilter(private val annotatedWith: Collection<String>?) : TestRunFilter {

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict =
        if (annotatedWith.isNullOrEmpty()) {
            TestRunFilter.Verdict.Run
        } else {
            if (test.annotations.any { annotation -> annotation.name in annotatedWith }) {
                TestRunFilter.Verdict.Run
            } else {
                TestRunFilter.Verdict.Skip.NotAnnotatedWith(annotatedWith)
            }
        }
}
