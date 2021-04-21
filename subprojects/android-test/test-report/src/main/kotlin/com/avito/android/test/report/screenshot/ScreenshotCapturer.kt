package com.avito.android.test.report.screenshot

import com.avito.android.Result
import java.io.File
import java.util.UUID
import android.graphics.Bitmap as AndroidBitmap

interface ScreenshotCapturer {

    fun captureBitmap(): Result<Capture>

    /**
     * @return null when no Activity in RESUMED state
     */
    fun captureAsFile(
        filename: String = "${UUID.randomUUID()}.png",
        compressFormat: AndroidBitmap.CompressFormat = AndroidBitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Result<File?>

    sealed class Capture {
        class Bitmap(val value: AndroidBitmap) : Capture()
        object NoActivity : Capture()
    }
}
