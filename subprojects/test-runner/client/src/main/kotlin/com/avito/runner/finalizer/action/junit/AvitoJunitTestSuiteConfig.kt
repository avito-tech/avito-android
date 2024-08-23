package com.avito.runner.finalizer.action.junit

import com.avito.junit.JunitTestSuiteConfig
import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.test.model.TestName

internal class AvitoJunitTestSuiteConfig(
    testSuiteNameProvider: TestSuiteNameProvider,
    private val reportLinksGenerator: ReportLinksGenerator,
) : JunitTestSuiteConfig {

    override val testSuiteName: String = testSuiteNameProvider.getName()

    override fun getTestReportLink(name: TestName): String {
        return reportLinksGenerator.generateTestLink(name)
    }
}
