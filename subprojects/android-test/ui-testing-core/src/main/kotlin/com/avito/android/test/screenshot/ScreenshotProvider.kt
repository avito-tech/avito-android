@file:Suppress("DEPRECATION")

package com.avito.android.test.screenshot

import android.graphics.Bitmap
import androidx.test.runner.screenshot.Screenshot

public interface ScreenshotProvider {

    public fun getScreenshot(): Bitmap
}

public class SupportTestScreenshotProvider : ScreenshotProvider {

    override fun getScreenshot(): Bitmap = Screenshot.capture().bitmap
}
