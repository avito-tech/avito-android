package com.avito.junit

import com.avito.test.model.TestName

public interface JunitTestSuiteConfig {
    public val testSuiteName: String

    public fun getTestReportLink(name: TestName): String
}
