package com.avito.instrumentation.internal.report.listener

import com.avito.instrumentation.internal.logcat.LogcatAccessor
import com.avito.report.model.Entry
import com.avito.retrace.ProguardRetracer

internal interface LogcatProcessor {

    suspend fun process(logcatAccessor: LogcatAccessor, isUploadNeeded: Boolean): String

    class Impl(
        private val testArtifactsUploader: TestArtifactsUploader,
        private val retracer: ProguardRetracer
    ) : LogcatProcessor {

        override suspend fun process(logcatAccessor: LogcatAccessor, isUploadNeeded: Boolean): String {
            return when {
                !isUploadNeeded -> "logcat not uploaded"
                !logcatAccessor.isAvailable -> "logcat not available"
                else -> testArtifactsUploader.upload(
                    content = retracer.retrace(logcatAccessor.getLogs().output),
                    type = Entry.File.Type.plain_text
                ).fold(
                    onSuccess = { it.toString() },
                    onFailure = { "Can't upload logcat: ${it.message}" }
                )
            }
        }
    }
}
