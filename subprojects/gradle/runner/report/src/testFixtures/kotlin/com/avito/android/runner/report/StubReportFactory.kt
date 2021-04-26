package com.avito.android.runner.report

import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider

public class StubReportFactory(
    private val report: StubReport = StubReport(),
    private val linkGenerator: ReportLinkGenerator = ReportLinkGenerator.NoOp(),
    private val nameProvider: TestSuiteNameProvider = TestSuiteNameProvider.NoOp()
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
