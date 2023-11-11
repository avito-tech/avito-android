package com.avito.runner.scheduler.report

import com.avito.android.Result
import com.avito.report.Report
import com.avito.report.inmemory.InMemoryReportFactory
import com.avito.runner.listener.ReportArtifactsTestListenerProvider
import com.avito.runner.model.TestCaseRun
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.device.Device
import com.avito.test.model.TestCase
import java.io.File

internal class NoOpReportModule(
    dependencies: ReportModuleDependencies,
) : ReportModule {

    override val report: Report = ReportImpl(
        inMemoryReport = InMemoryReportFactory(
            timeProvider = dependencies.timeProvider,
            loggerFactory = dependencies.loggerFactory,
        ).createReport(),
        externalReportService = null
    )
    override val artifactsTestListenerProvider =
        ReportArtifactsTestListenerProvider {
            object : TestListener {
                override fun started(device: Device, targetPackage: String, test: TestCase, executionNumber: Int) {
                    // no-op
                }

                override fun finished(
                    device: Device,
                    test: TestCase,
                    targetPackage: String,
                    result: TestCaseRun.Result,
                    durationMilliseconds: Long,
                    executionNumber: Int,
                    testArtifactsDir: Result<File>
                ) {
                    // no-op
                }
            }
        }
}
