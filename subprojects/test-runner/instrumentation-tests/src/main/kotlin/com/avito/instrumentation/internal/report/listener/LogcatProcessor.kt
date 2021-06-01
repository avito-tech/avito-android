package com.avito.instrumentation.internal.report.listener

import com.avito.android.asPlainText
import com.avito.instrumentation.internal.logcat.LogcatAccessor
import com.avito.instrumentation.internal.logcat.LogcatResult
import com.avito.report.model.Entry
import com.avito.retrace.ProguardRetracer

internal interface LogcatProcessor {

    suspend fun process(
        logcatAccessor: LogcatAccessor,
        isUploadNeeded: Boolean
    ): String

    class Impl(
        private val testArtifactsUploader: TestArtifactsUploader,
        private val retracer: ProguardRetracer
    ) : LogcatProcessor {

        override suspend fun process(
            logcatAccessor: LogcatAccessor,
            isUploadNeeded: Boolean
        ): String {
            return when {
                !isUploadNeeded -> "logcat not uploaded"
                else -> when (val logcatResult = logcatAccessor.getLogs()) {
                    is LogcatResult.Unavailable -> "Logcat is not available:\n${logcatResult.reason.asPlainText()}"
                    is LogcatResult.Success -> testArtifactsUploader.upload(
                        content = retracer.retrace(logcatResult.output),
                        type = Entry.File.Type.plain_text
                    ).fold(
                        onSuccess = { it.toString() },
                        onFailure = { "Can't upload logcat: ${it.message}" }
                    )
                }
            }
        }
    }
}
