package com.avito.android.test.screenshot

import android.graphics.Bitmap
import androidx.test.runner.screenshot.Screenshot

interface ScreenshotProvider {

    fun getScreenshot(): Bitmap
}

class SupportTestScreenshotProvider : ScreenshotProvider {

    override fun getScreenshot(): Bitmap = Screenshot.capture().bitmap
}
