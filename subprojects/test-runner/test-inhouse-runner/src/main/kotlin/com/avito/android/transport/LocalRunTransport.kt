package com.avito.android.transport

import android.os.Looper
import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.transport.TransportMappers
import com.avito.filestorage.FutureValue
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportViewer
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.DeviceName
import com.avito.report.model.Entry
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestName
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticDataPackage
import java.io.File

internal class LocalRunTransport(
    reportViewerUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val deviceName: DeviceName,
    loggerFactory: LoggerFactory,
    private val reportsApi: ReportsApi,
    private val remoteStorageTransport: AvitoRemoteStorageTransport
) : Transport, TransportMappers {

    private val logger = loggerFactory.create<LocalRunTransport>()

    private val localBuildId: String? = null

    private val reportViewer: ReportViewer = ReportViewer.Impl(reportViewerUrl, reportCoordinates)

    override fun sendReport(state: Started) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Thread { sendInternal(state) }.apply {
                start()
                join()
            }
        } else {
            sendInternal(state)
        }
    }

    private fun sendInternal(state: Started) {
        try {
            val testName = TestName(
                className = state.testMetadata.className,
                methodName = state.testMetadata.methodName!!
            )

            val testStaticData = TestStaticDataPackage(
                name = testName,
                device = deviceName,
                description = state.testMetadata.description,
                testCaseId = state.testMetadata.caseId,
                dataSetNumber = state.testMetadata.dataSetNumber,
                externalId = state.testMetadata.externalId,
                tagIds = state.testMetadata.tagIds,
                featureIds = state.testMetadata.featureIds,
                priority = state.testMetadata.priority,
                behavior = state.testMetadata.behavior,
                kind = state.testMetadata.kind,
                flakiness = state.testMetadata.flakiness
            )

            val result = reportsApi.addTest(
                reportCoordinates = reportCoordinates,
                buildId = localBuildId,
                test = AndroidTest.Completed.create(
                    testStaticData = testStaticData,
                    testRuntimeData = TestRuntimeDataPackage(
                        incident = state.incident,
                        dataSetData = state.dataSet?.serialize() ?: emptyMap(),
                        video = state.video,
                        preconditions = transformStepList(state.preconditionStepList),
                        steps = transformStepList(state.testCaseStepList),
                        startTime = state.startTime,
                        endTime = state.endTime
                    ),
                    // local runs already has logcat in place
                    stdout = "",
                    stderr = ""
                )
            )

            logger.info(
                "Report link for test ${testName.name}: " +
                    "${reportViewer.generateSingleTestRunUrl(result.getOrThrow())}"
            )

            @Suppress("ControlFlowWithEmptyBody")
            if (reportCoordinates.runId.contains("local", ignoreCase = true)) {
                // todo find a way to display info in user context, it's a secret knowledge about logcat line
            }
        } catch (e: Exception) {
            logger.warn("Report send failed", e)
        }
    }

    override fun sendContent(
        test: TestMetadata,
        file: File,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return remoteStorageTransport.sendContent(test, file, type, comment)
    }

    override fun sendContent(
        test: TestMetadata,
        content: String,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return remoteStorageTransport.sendContent(test, content, type, comment)
    }
}
