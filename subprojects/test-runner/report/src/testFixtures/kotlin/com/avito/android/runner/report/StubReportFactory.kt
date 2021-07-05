package com.avito.android.runner.report

import com.avito.report.NoOpReportLinksGenerator
import com.avito.report.NoOpTestSuiteNameProvider
import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider

public class StubReportFactory(
    private val report: StubReport = StubReport(),
    private val linksGenerator: ReportLinksGenerator = NoOpReportLinksGenerator(),
    private val nameProvider: TestSuiteNameProvider = NoOpTestSuiteNameProvider()
) : ReportFactory {

    override fun createReport(): Report {
        return report
    }

    override fun createAvitoReport(): LegacyReport {
        return report
    }

    override fun createReportLinkGenerator(): ReportLinksGenerator {
        return linksGenerator
    }

    override fun createTestSuiteNameGenerator(): TestSuiteNameProvider {
        return nameProvider
    }
}
