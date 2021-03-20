package com.avito.android.test.report.screenshot

import android.graphics.Bitmap
import com.avito.android.Result
import java.io.File
import java.io.InputStream
import java.util.UUID

interface ScreenshotCapturer {

    fun captureBitmap(): Result<Bitmap>

    fun captureAsStream(): Result<InputStream>

    fun captureAsFile(
        filename: String = "${UUID.randomUUID()}.png",
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Result<File>
}
