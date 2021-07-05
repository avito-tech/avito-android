package com.avito.android.runner.report

import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider
import java.io.Serializable

public interface ReportFactory : Serializable {

    public fun createReport(): Report

    public fun createReportLinkGenerator(): ReportLinksGenerator

    public fun createTestSuiteNameGenerator(): TestSuiteNameProvider
}
