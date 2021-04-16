package com.avito.android.runner.report

import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider

public interface ReportFactory {

    public fun createReport(): Report

    public fun createReadReport(): ReadReport

    public fun createAvitoReport(): AvitoReport

    public fun createReportLinkGenerator(): ReportLinkGenerator

    public fun createTestSuiteNameGenerator(): TestSuiteNameProvider
}
