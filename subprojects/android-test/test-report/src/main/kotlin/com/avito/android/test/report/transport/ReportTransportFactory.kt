package com.avito.android.test.report.transport

import com.avito.filestorage.RemoteStorage
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.report.ReportsApiFactory
import com.avito.report.model.DeviceName
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.ReportCoordinates
import com.avito.time.TimeProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class ReportTransportFactory(
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    private val remoteStorage: RemoteStorage,
    private val httpClientProvider: HttpClientProvider
) {

    fun create(
        testRunCoordinates: ReportCoordinates,
        reportDestination: ReportDestination
    ): Transport {

        val gson: Gson = GsonBuilder()
            .registerTypeAdapterFactory(EntryTypeAdapterFactory())
            .create()

        val externalStorageTransport = ExternalStorageTransport(
            gson = gson,
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        )

        val uploadFromDevice = AvitoRemoteStorageTransport(remoteStorage)

        return when (reportDestination) {
            is ReportDestination.Backend -> LocalRunTransport(
                reportViewerUrl = reportDestination.reportViewerUrl,
                reportCoordinates = testRunCoordinates,
                deviceName = DeviceName(reportDestination.deviceName),
                loggerFactory = loggerFactory,
                reportsApi = ReportsApiFactory.create(
                    host = reportDestination.reportApiUrl,
                    loggerFactory = loggerFactory,
                    httpClientProvider = httpClientProvider
                ),
                remoteStorageTransport = uploadFromDevice
            )
            is ReportDestination.Legacy -> LegacyTransport(
                remoteStorageTransport = uploadFromDevice,
                externalStorageTransport = externalStorageTransport
            )
            ReportDestination.File -> externalStorageTransport
            ReportDestination.NoOp -> StubTransport
        }
    }
}
