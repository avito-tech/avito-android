package com.avito.android.runner.report

import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider
import java.io.Serializable

public interface ReportFactory : Serializable {

    public fun createReport(): Report

    public fun createAvitoReport(): LegacyReport

    public fun createReportLinkGenerator(): ReportLinkGenerator

    public fun createTestSuiteNameGenerator(): TestSuiteNameProvider
}
