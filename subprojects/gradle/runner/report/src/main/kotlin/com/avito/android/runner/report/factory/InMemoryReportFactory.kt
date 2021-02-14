package com.avito.android.runner.report.factory

import com.avito.android.runner.report.InMemoryReport
import com.avito.android.runner.report.ReadReport
import com.avito.android.runner.report.Report
import com.avito.time.TimeProvider

public class InMemoryReportFactory(
    private val timeProvider: TimeProvider
) : ReportFactory {

    @Transient
    private var reports: MutableMap<ReportFactory.Config.InMemory, InMemoryReport> = mutableMapOf()

    // TODO problems with serialization
    @Synchronized
    override fun createReport(config: ReportFactory.Config): Report {
        return when (config) {
            is ReportFactory.Config.InMemory -> reports.getOrPut(config, {
                InMemoryReport(
                    id = config.id,
                    timeProvider = timeProvider
                )
            })
            is ReportFactory.Config.ReportViewerCoordinates -> TODO("Unsupported type")
            is ReportFactory.Config.ReportViewerId -> TODO("Unsupported type")
        }
    }

    @Synchronized
    override fun createReadReport(config: ReportFactory.Config): ReadReport {
        return when (config) {
            is ReportFactory.Config.InMemory -> reports.getOrPut(config, {
                InMemoryReport(
                    id = config.id,
                    timeProvider = timeProvider
                )
            })
            is ReportFactory.Config.ReportViewerCoordinates -> TODO("Unsupported type")
            is ReportFactory.Config.ReportViewerId -> TODO("Unsupported type")
        }
    }
}
