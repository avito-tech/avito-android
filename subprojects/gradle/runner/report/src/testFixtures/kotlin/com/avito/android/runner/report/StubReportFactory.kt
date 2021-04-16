package com.avito.android.runner.report

import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider

public object StubReportFactory : ReportFactory {

    override fun createReport(): Report {
        return StubReport()
    }

    override fun createReadReport(): ReadReport {
        return StubReport()
    }

    override fun createAvitoReport(): AvitoReport {
        return StubReport()
    }

    override fun createReportLinkGenerator(): ReportLinkGenerator {
        return ReportLinkGenerator.Stub
    }

    override fun createTestSuiteNameGenerator(): TestSuiteNameProvider {
        return TestSuiteNameProvider.Stub
    }
}
