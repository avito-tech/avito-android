package com.avito.android.test.report.screenshot

import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.Logger
import java.io.File

interface ScreenshotUploader {

    fun makeAndUploadScreenshot(
        comment: String = "Screenshot"
    ): FutureValue<RemoteStorage.Result>?

    class Impl(
        private val remoteStorage: RemoteStorage,
        private val screenshotCapturer: ScreenshotCapturer,
        private val logger: Logger
    ) : ScreenshotUploader {

        override fun makeAndUploadScreenshot(comment: String): FutureValue<RemoteStorage.Result>? {

            // используем файл а не inputStream, потому что okhttp может понадобиться несколько раз отправить файл (retry)
            val screenshot: File = try {
                screenshotCapturer.captureToFile()
            } catch (e: IllegalStateException) {
                logger.exception("Unable to make screenshot: ${e.message}", e)
                return null
            }

            return remoteStorage.upload(
                uploadRequest = RemoteStorage.Request.FileRequest.Image(
                    file = screenshot
                ),
                comment = comment
            )
        }
    }
}
