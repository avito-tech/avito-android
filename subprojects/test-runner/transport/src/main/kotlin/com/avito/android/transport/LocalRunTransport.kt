package com.avito.android.transport

import com.avito.android.Result
import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.transport.TransportMappers
import com.avito.filestorage.FutureValue
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportLinksGenerator
import com.avito.report.model.AndroidTest
import com.avito.report.model.Entry
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticDataPackage
import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.ReportViewerQuery
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.model.DeviceName
import java.io.File

internal class LocalRunTransport(
    reportViewerUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val deviceName: DeviceName,
    loggerFactory: LoggerFactory,
    private val reportsApi: ReportsApi,
    reportViewerQuery: ReportViewerQuery,
    private val remoteStorageTransport: AvitoRemoteStorageTransport
) : Transport, TransportMappers {

    private val logger = loggerFactory.create<LocalRunTransport>()

    private val localBuildId: String? = null

    private val reportLinksGenerator: ReportLinksGenerator = ReportViewerLinksGeneratorImpl(
        reportViewerUrl = reportViewerUrl,
        reportCoordinates = reportCoordinates,
        reportViewerQuery = reportViewerQuery
    )

    override fun sendReport(state: Started) {
        Thread { sendInternal(state) }.apply {
            start()
            join()
        }
    }

    private fun sendInternal(state: Started) {
        Result.tryCatch {
            val testStaticData = TestStaticDataPackage(
                name = state.testMetadata.name,
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

            AndroidTest.Completed.create(
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
                logcat = ""
            )
        }.map { test ->
            reportsApi.addTest(
                reportCoordinates = reportCoordinates,
                buildId = localBuildId,
                test = test
            )
            test
        }.fold(
            onSuccess = { test ->
                logger.info(
                    "Report link for test ${test.name}: ${reportLinksGenerator.generateTestLink(test.name)}"
                )
            },
            onFailure = { throwable ->
                logger.warn("Report send failed", throwable)
            }
        )
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
