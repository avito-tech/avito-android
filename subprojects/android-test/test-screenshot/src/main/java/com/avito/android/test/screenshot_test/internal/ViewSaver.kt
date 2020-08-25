package com.avito.android.test.screenshot_test.internal

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.O)
internal fun View.saveScreenshot(activity: Activity, screenshotFileName: String) {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val locationOfViewInWindow = IntArray(2)
    val window = activity.window
    getLocationInWindow(locationOfViewInWindow)
    PixelCopy.request(
        window,
        Rect(
            locationOfViewInWindow[0],
            locationOfViewInWindow[1],
            locationOfViewInWindow[0] + width,
            locationOfViewInWindow[1] + height
        ), bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                saveToSdCard(screenshotFileName, bitmap)
            } else {
                throw IllegalStateException("Can't make PixelCopy request")
            }
        },
        Handler(activity.mainLooper)
    )
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
    val screenshotFilePath = "${dir.path}/$name.png"
    FileOutputStream(screenshotFilePath).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        Log.i("ViewSaver", "successfully save screenshot to $screenshotFilePath")
    }
}
