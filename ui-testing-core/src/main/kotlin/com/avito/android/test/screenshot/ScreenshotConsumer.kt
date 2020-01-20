package com.avito.android.test.screenshot

import android.graphics.Bitmap

interface ScreenshotConsumer {

    fun onScreenshotIsReady(screenshot: Bitmap, description: String)
}
