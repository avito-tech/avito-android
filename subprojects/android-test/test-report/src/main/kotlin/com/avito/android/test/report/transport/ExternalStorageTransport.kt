package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import com.google.gson.Gson

/**
 * Save report to json that will be parsed from runner
 */
class ExternalStorageTransport(
    private val gson: Gson,
    timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : Transport, PreTransportMappers {

    private val logger = loggerFactory.create<ExternalStorageTransport>()

    private val outputFileProvider = OutputFileProvider()
    private val testRuntimeDataBuilder = TestRuntimeDataBuilder(timeProvider)

    override fun send(state: ReportState.Initialized.Started) {
        outputFileProvider.provideReportFile(
            className = state.testMetadata.className,
            methodName = state.testMetadata.methodName!!
        ).fold(
            onSuccess = { file ->
                try {
                    val json = gson.toJson(testRuntimeDataBuilder.fromState(state))
                    file.writeText(json)
                    logger.info("Wrote report for test to file: ${file.absolutePath}")
                } catch (e: Throwable) {
                    logger.critical("Can't write report runtime data; leads to LOST test", e)
                }
            },
            onFailure = { throwable ->
                logger.critical(
                    "Can't create output file for test runtime data; leads to LOST test",
                    throwable
                )
            }
        )
    }
}
