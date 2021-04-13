package com.avito.android.runner.report.factory

import com.avito.android.runner.report.InMemoryReport
import com.avito.android.runner.report.ReadReport
import com.avito.android.runner.report.Report
import com.avito.time.TimeProvider

public class InMemoryReportFactory(
    private val timeProvider: TimeProvider
) : LegacyReportFactory {

    @Transient
    private var reports: MutableMap<LegacyReportFactory.Config.InMemory, InMemoryReport> = mutableMapOf()

    // TODO problems with serialization
    @Synchronized
    override fun createReport(config: LegacyReportFactory.Config): Report {
        return when (config) {
            is LegacyReportFactory.Config.InMemory -> reports.getOrPut(config, {
                InMemoryReport(
                    id = config.id,
                    timeProvider = timeProvider
                )
            })
            is LegacyReportFactory.Config.ReportViewerCoordinates -> TODO("Unsupported type")
            is LegacyReportFactory.Config.ReportViewerId -> TODO("Unsupported type")
        }
    }

    @Synchronized
    override fun createReadReport(config: LegacyReportFactory.Config): ReadReport {
        return when (config) {
            is LegacyReportFactory.Config.InMemory -> reports.getOrPut(config, {
                InMemoryReport(
                    id = config.id,
                    timeProvider = timeProvider
                )
            })
            is LegacyReportFactory.Config.ReportViewerCoordinates -> TODO("Unsupported type")
            is LegacyReportFactory.Config.ReportViewerId -> TODO("Unsupported type")
        }
    }
}
