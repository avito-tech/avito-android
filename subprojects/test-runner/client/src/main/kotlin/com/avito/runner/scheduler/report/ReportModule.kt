package com.avito.runner.scheduler.report

import com.avito.report.Report
import com.avito.runner.config.RunnerReportConfig
import com.avito.runner.listener.ReportArtifactsTestListenerProvider

internal interface ReportModule {
    val report: Report
    val artifactsTestListenerProvider: ReportArtifactsTestListenerProvider

    companion object {
        fun create(
            reportConfig: RunnerReportConfig,
            dependencies: ReportModuleDependencies,
        ): ReportModule {
            return when (reportConfig) {
                RunnerReportConfig.None -> NoOpReportModule(dependencies)
                is RunnerReportConfig.ReportViewer -> ReportViewerReportModule(
                    reportConfig = reportConfig,
                    dependencies = dependencies,
                )
            }
        }
    }
}
