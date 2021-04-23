package com.avito.android.test.screenshot_test.internal

import android.content.Context
import com.avito.android.test.report.Report
import com.avito.composite_exception.composeWith
import com.avito.filestorage.RemoteStorage
import com.avito.filestorage.RemoteStorageRequest
import com.avito.filestorage.RemoteStorageResult
import okhttp3.HttpUrl

internal class ScreenshotComparisonReporter(
    private val remoteStorage: RemoteStorage,
    private val report: Report,
    private val context: Context
) {

    private val RemoteStorageResult.error: Throwable?
        get() = (this as? RemoteStorageResult.Error)?.t

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
        val generatedScreenshotResult = generatedScreenshotFuture.get()
        val referenceScreenshotResult = referenceScreenshotFuture.get()
        if (generatedScreenshotResult is RemoteStorageResult.Success
            && referenceScreenshotResult is RemoteStorageResult.Success
        ) {
            val htmlReport = getReportAsString(
                referenceUrl = referenceScreenshotResult.url,
                generatedUrl = generatedScreenshotResult.url
            )
            report.addHtml(
                label = "Press me to see report",
                content = htmlReport,
                wrapHtml = false
            )
        } else {
            throw IllegalStateException(
                "Can't upload screenshots",
                generatedScreenshotResult.error.composeWith(referenceScreenshotResult.error)
            )
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
