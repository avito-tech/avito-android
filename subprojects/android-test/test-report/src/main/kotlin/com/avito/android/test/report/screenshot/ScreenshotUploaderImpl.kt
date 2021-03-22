package com.avito.android.test.report.screenshot

import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.LoggerFactory
import com.avito.logger.create

class ScreenshotUploaderImpl(
    private val remoteStorage: RemoteStorage,
    private val screenshotCapturer: ScreenshotCapturer,
    loggerFactory: LoggerFactory
) : ScreenshotUploader {

    private val logger = loggerFactory.create<ScreenshotUploader>()

    override fun makeAndUploadScreenshot(comment: String): FutureValue<RemoteStorage.Result>? {

        // using file, and not an inputStream, because okhttp could use it multiple times (retries)
        return screenshotCapturer.captureAsFile().fold(
            { screenshot ->
                if (screenshot != null) {
                    remoteStorage.upload(
                        uploadRequest = RemoteStorage.Request.FileRequest.Image(
                            file = screenshot
                        ),
                        comment = comment
                    )
                } else {
                    null
                }
            },
            { error ->
                logger.warn("Unable to make screenshot: ${error.message}", error)
                null
            }
        )
    }
}
