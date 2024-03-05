package com.avito.android.test.screenshot

import android.graphics.Bitmap

public interface ScreenshotConsumer {

    public fun onScreenshotIsReady(screenshot: Bitmap, description: String)
}
