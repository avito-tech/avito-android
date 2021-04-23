package com.avito.android.test.screenshot_test.internal

import android.content.Context
import com.avito.android.test.report.Report
import com.avito.filestorage.RemoteStorage
import com.avito.filestorage.RemoteStorageRequest
import okhttp3.HttpUrl

internal class ScreenshotComparisonReporter(
    private val remoteStorage: RemoteStorage,
    private val report: Report,
    private val context: Context
) {

    fun reportScreenshotComparison(
        generated: Screenshot,
        reference: Screenshot
    ) {
        val generatedScreenshotFuture = remoteStorage.upload(
            RemoteStorageRequest.FileRequest.Image(
                file = generated.path
            ),
            comment = "generated screenshot"
        )
        val referenceScreenshotFuture = remoteStorage.upload(
            RemoteStorageRequest.FileRequest.Image(
                file = reference.path
            ),
            comment = "reference screenshot"
        )

        referenceScreenshotFuture.get().combine(generatedScreenshotFuture.get()) { referenceUrl, generatedUrl ->
            val htmlReport = getReportAsString(
                referenceUrl = referenceUrl,
                generatedUrl = generatedUrl
            )
            report.addHtml(
                label = "Press me to see report",
                content = htmlReport,
                wrapHtml = false
            )
        }.onFailure {
            throw IllegalStateException("Can't upload screenshots", it)
        }
    }

    private fun getReportAsString(referenceUrl: HttpUrl, generatedUrl: HttpUrl): String {
        context.assets.open("screenshot_test_report.html").use {
            val size: Int = it.available()
            val buffer = ByteArray(size)
            it.read(buffer)
            return String(buffer)
                .replace("%referenceImage%", referenceUrl.toString())
                .replace("%generatedImage%", generatedUrl.toString())
        }
    }
}
