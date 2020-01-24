package com.avito.android.test.report.transport

import android.os.Looper
import com.avito.android.test.report.ReportState
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.DeviceName
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestName
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticDataPackage

class LocalRunTransport(
    reportApiHost: String,
    reportFallbackUrl: String,
    private val reportResultEndpoint: String,
    private val reportCoordinates: ReportCoordinates,
    private val deviceName: DeviceName,
    private val logger: (String, Throwable?) -> Unit,
    private val reportsApi: ReportsApi = ReportsApi.create(
        //todo пробросить параметром
        host = reportApiHost,
        fallbackUrl = reportFallbackUrl,
        logger = logger
    )
) : Transport, PreTransportMappers {

    private val localBuildId: String? = null

    override fun send(state: ReportState.Initialized.Started) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Thread { sendInternal(state) }.apply {
                start()
                join()
            }
        } else {
            sendInternal(state)
        }
    }

    private fun sendInternal(state: ReportState.Initialized.Started) {
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
                priority = state.testMetadata.priority,
                behavior = state.testMetadata.behavior,
                kind = state.testMetadata.kind,
                features = combineFeatures(state.testMetadata)
            )

            val result = reportsApi.addTest(
                reportCoordinates = reportCoordinates,
                buildId = localBuildId,
                test = AndroidTest.Completed.create(
                    testStaticData = testStaticData,
                    testRuntimeData = TestRuntimeDataPackage(
                        incident = state.incident,
                        dataSetData = state.dataSet?.serialize() ?: emptyMap(),
                        performanceJson = state.performanceJson,
                        video = state.video,
                        preconditions = transformStepList(state.preconditionStepList),
                        steps = transformStepList(state.testCaseStepList),
                        startTime = state.startTime,
                        endTime = state.endTime
                    ),
                    //для локального запуска смотрим в logcat
                    stdout = "",
                    stderr = ""
                )
            )

            logger("Report sent $result", null)
            logger(
                "Report link for test ${testName.name}: $reportResultEndpoint/${result.get()}",
                null
            )

            if (reportCoordinates.runId.contains("local", ignoreCase = true)) {
                //todo как-то на девайсе отобразить что ссылка на отчет ждет вас в logcat
            }

        } catch (e: Exception) {
            logger("Report send failed", e)
        }
    }

    companion object {
        const val TAG = "TestWarehouseReporter"
    }
}
