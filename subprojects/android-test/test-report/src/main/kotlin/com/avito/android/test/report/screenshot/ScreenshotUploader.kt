package com.avito.android.test.report.screenshot

import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage

interface ScreenshotUploader {

    fun makeAndUploadScreenshot(
        comment: String = "Screenshot"
    ): FutureValue<RemoteStorage.Result>?
}
