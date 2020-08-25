package com.avito.android.test.screenshot_test.internal

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.view.PixelCopy
import android.view.View
import androidx.annotation.RequiresApi


internal class ViewScreenshotMaker(
    private val activity: Activity,
    private val screenshot: Screenshot,
    private val bitmapSaver: BitmapSaver = BitmapSaver()
) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun makeScreenshot(view: View) {
        val width = view.width
        val height = view.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        val window = activity.window
        view.getLocationInWindow(locationOfViewInWindow)
        PixelCopy.request(
            window,
            Rect(
                locationOfViewInWindow[0],
                locationOfViewInWindow[1],
                locationOfViewInWindow[0] + width,
                locationOfViewInWindow[1] + height
            ), bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    bitmapSaver.save(bitmap, screenshot)
                } else {
                    throw IllegalStateException("Can't make PixelCopy request")
                }
            },
            Handler(activity.mainLooper)
        )
    }
}

internal class BitmapSaver {
    fun save(bitmap: Bitmap, screenshot: Screenshot) {
        saveBitmap(bitmap, screenshot.path)
    }
}
