package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName

class IgnoredAnnotationFilter(
    private val ignoreAnnotations: Set<String> = setOf(
        "org.junit.Ignore"
    )
) : TestRunFilter {

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict =
        if (test.annotations.any { annotation -> annotation.name in ignoreAnnotations }) {
            TestRunFilter.Verdict.Skip.Ignored
        } else {
            TestRunFilter.Verdict.Run
        }
}
