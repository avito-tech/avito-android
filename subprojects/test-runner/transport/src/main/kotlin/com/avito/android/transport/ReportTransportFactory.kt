package com.avito.android.transport

import com.avito.android.test.report.transport.ExternalStorageTransport
import com.avito.android.test.report.transport.NoOpTransport
import com.avito.android.test.report.transport.Transport
import com.avito.filestorage.RemoteStorageFactory
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.TestArtifactsProvider
import com.avito.report.serialize.ReportSerializer
import com.avito.reportviewer.ReportViewerQuery
import com.avito.reportviewer.ReportsApiFactory
import com.avito.test.model.DeviceName
import com.avito.time.TimeProvider
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

public class ReportTransportFactory(
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    private val okHttpClientBuilder: OkHttpClient.Builder,
    private val reportViewerQuery: ReportViewerQuery,
    reportSerializer: ReportSerializer,
    testArtifactsProvider: TestArtifactsProvider
) {

    private val logger = loggerFactory.create<ReportTransportFactory>()

    private val externalStorageTransport = ExternalStorageTransport(
        timeProvider = timeProvider,
        loggerFactory = loggerFactory,
        testArtifactsProvider = testArtifactsProvider,
        reportSerializer = reportSerializer
    )

    public fun create(
        reportDestination: ReportDestination
    ): Transport {

        logger.info("reportDestination=$reportDestination")

        return when (reportDestination) {
            is ReportDestination.Backend -> LocalRunTransport(
                reportViewerUrl = reportDestination.reportViewerUrl,
                reportCoordinates = reportDestination.coordinates,
                deviceName = DeviceName(reportDestination.deviceName),
                loggerFactory = loggerFactory,
                reportsApi = ReportsApiFactory.create(
                    host = reportDestination.reportApiUrl,
                    builder = okHttpClientBuilder,
                    loggerFactory = loggerFactory,
                ),
                reportViewerQuery = reportViewerQuery,
                remoteStorageTransport = createRemoteStorageTransport(reportDestination.fileStorageUrl)
            )
            is ReportDestination.Legacy -> LegacyTransport(
                remoteStorageTransport = createRemoteStorageTransport(reportDestination.fileStorageUrl),
                externalStorageTransport = externalStorageTransport
            )
            ReportDestination.File -> externalStorageTransport
            ReportDestination.NoOp -> NoOpTransport
        }
    }

    private fun createRemoteStorageTransport(fileStorage: HttpUrl): AvitoRemoteStorageTransport {
        val remoteStorage = RemoteStorageFactory.create(
            endpoint = fileStorage,
            builder = okHttpClientBuilder,
            isAndroidRuntime = true
        )
        return AvitoRemoteStorageTransport(remoteStorage, timeProvider)
    }
}
