package com.avito.android.runner.report

import com.avito.report.NoOpReportLinkGenerator
import com.avito.report.NoOpTestSuiteNameProvider
import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider

public class StubReportFactory(
    private val report: StubReport = StubReport(),
    private val linkGenerator: ReportLinkGenerator = NoOpReportLinkGenerator(),
    private val nameProvider: TestSuiteNameProvider = NoOpTestSuiteNameProvider()
) : ReportFactory {

    override fun createReport(): Report {
        return report
    }

    override fun createAvitoReport(): LegacyReport {
        return report
    }

    override fun createReportLinkGenerator(): ReportLinkGenerator {
        return linkGenerator
    }

    override fun createTestSuiteNameGenerator(): TestSuiteNameProvider {
        return nameProvider
    }
}
