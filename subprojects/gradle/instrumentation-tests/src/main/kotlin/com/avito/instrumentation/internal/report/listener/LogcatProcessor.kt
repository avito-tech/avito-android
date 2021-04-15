package com.avito.instrumentation.internal.report.listener

import com.avito.report.model.Entry
import com.avito.retrace.ProguardRetracer

internal interface LogcatProcessor {

    suspend fun process(logcat: List<String>?, isUploadNeeded: Boolean): String

    class Impl(
        private val testArtifactsUploader: TestArtifactsUploader,
        private val retracer: ProguardRetracer
    ) : LogcatProcessor {

        override suspend fun process(logcat: List<String>?, isUploadNeeded: Boolean): String {
            return when {
                !isUploadNeeded -> "logcat not uploaded"
                logcat == null -> "logcat not available"
                else -> testArtifactsUploader.upload(
                    content = retracer.retrace(logcat.joinToString(separator = "\n")),
                    type = Entry.File.Type.plain_text
                ).fold(
                    onSuccess = { it.toString() },
                    onFailure = { "Can't upload logcat: ${it.message}" }
                )
            }
        }
    }
}
