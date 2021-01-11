package com.avito.android.test.report.screenshot

import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.LoggerFactory
import com.avito.logger.create

interface ScreenshotUploader {

    fun makeAndUploadScreenshot(
        comment: String = "Screenshot"
    ): FutureValue<RemoteStorage.Result>?

    class Impl(
        private val remoteStorage: RemoteStorage,
        private val screenshotCapturer: ScreenshotCapturer,
        loggerFactory: LoggerFactory
    ) : ScreenshotUploader {

        private val logger = loggerFactory.create<ScreenshotUploader>()

        override fun makeAndUploadScreenshot(comment: String): FutureValue<RemoteStorage.Result>? {

            // используем файл а не inputStream,
            // потому что okhttp может понадобиться несколько раз отправить файл (retry)
            return screenshotCapturer.captureAsFile().fold(
                { optionalScreenshot ->
                    optionalScreenshot.fold(
                        // it's ok because there may not be a RESUMED activity
                        ifEmpty = { null },
                        some = { screenshot ->
                            remoteStorage.upload(
                                uploadRequest = RemoteStorage.Request.FileRequest.Image(
                                    file = screenshot
                                ),
                                comment = comment
                            )
                        }
                    )
                },
                { error ->
                    logger.warn("Unable to make screenshot: ${error.message}", error)
                    null
                }
            )
        }
    }
}
