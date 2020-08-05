package com.avito.android.test.screenshot_test.internal

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.PixelCopy
import android.view.View
import com.avito.android.test.screenshot_test.test.IdlieableActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal fun View.saveScreenshot(activity: IdlieableActivity, name: String) {
    val bitmap = Bitmap.createBitmap(width,
        height,
        Bitmap.Config.ARGB_8888)
    val locationOfViewInWindow = IntArray(2)
    val window = activity.window
    activity.countingIdlingResource.increment()
    getLocationInWindow(locationOfViewInWindow)
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PixelCopy.request(
                window,
                Rect(
                    locationOfViewInWindow[0],
                    locationOfViewInWindow[1],
                    locationOfViewInWindow[0] + width,
                    locationOfViewInWindow[1] + height
                ), bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    saveToSdCard(name, bitmap)
                } else {
                    Log.e(TAG, "cannot save image by PixelCopy")
                }
                activity.countingIdlingResource.decrement()
            },
                Handler(activity.mainLooper)
            )
        }
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "cannot save image", e)
        activity.countingIdlingResource.decrement()
    }
}

private fun View.saveToSdCard(name: String, bitmap: Bitmap) {
    val screenshotDirectory = ScreenshotDirectories(context.packageName)
    val deviceDirectoryName = DeviceDirectoryName.create(context)
    val dir = screenshotDirectory[deviceDirectoryName.name]
    dir.mkdirs()
    writeBitmap(dir, name, bitmap)
}

@Throws(IOException::class)
private fun writeBitmap(dir: File, name: String, bitmap: Bitmap) {
    val filenamePath = "${dir.path}/$name.png"
    val fileOutputStream = FileOutputStream(filenamePath)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
}

private const val TAG: String = "viewSaver"