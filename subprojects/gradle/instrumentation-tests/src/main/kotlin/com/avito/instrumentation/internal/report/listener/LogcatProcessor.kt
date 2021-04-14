package com.avito.instrumentation.internal.report.listener

import com.avito.report.model.Entry
import com.avito.retrace.ProguardRetracer

internal interface LogcatProcessor {

    suspend fun uploadLogcat(logcat: List<String>?, isUploadNeeded: Boolean): String

    class Impl(
        private val testArtifactsUploader: TestArtifactsUploader,
        private val retracer: ProguardRetracer
    ) : LogcatProcessor {

        override suspend fun uploadLogcat(logcat: List<String>?, isUploadNeeded: Boolean): String {
            return if (isUploadNeeded) {
                if (logcat != null) {
                    testArtifactsUploader.upload(
                        content = retracer.retrace(logcat.joinToString(separator = "\n")),
                        type = Entry.File.Type.plain_text
                    )
                        .fold(
                            onSuccess = { it.toString() },
                            onFailure = { "Can't upload logcat: ${it.message}" }
                        )
                } else {
                    "logcat not available"
                }
            } else {
                "logcat not uploaded"
            }
        }
    }
}
