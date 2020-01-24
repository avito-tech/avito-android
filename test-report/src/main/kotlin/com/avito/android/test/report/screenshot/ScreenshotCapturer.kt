package com.avito.android.test.report.screenshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import com.avito.android.test.util.getCurrentActivity
import com.avito.android.util.runOnMainThreadSync
import com.avito.android.util.toPng
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

interface ScreenshotCapturer {

    fun captureBitmap(): Bitmap

    fun captureAsStream(): InputStream

    fun captureToFile(
        filename: String = "${UUID.randomUUID()}.png",
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): File

    /**
     * Source: https://github.com/square/spoon/blob/master/spoon-client/src/main/java/com/squareup/spoon/Screenshot.java
     */
    class Impl(private val outputDirectory: Lazy<File>) : ScreenshotCapturer {

        override fun captureBitmap(): Bitmap {
            //todo use di: pass activity getter as constructor argument
            return drawCanvas(getCurrentActivity())
        }

        override fun captureAsStream(): InputStream {
            return captureBitmap().toPng()
        }

        override fun captureToFile(
            filename: String,
            compressFormat: Bitmap.CompressFormat,
            quality: Int
        ): File {
            val file = File(outputDirectory.value, filename)
            FileOutputStream(file).use {
                captureBitmap().compress(compressFormat, quality, it)
            }
            return file
        }

        private fun drawCanvas(activity: Activity): Bitmap {
            val view = activity.window.decorView

            if (view.width == 0 || view.height == 0) {
                throw IllegalStateException(
                    "Your view has no height or width. Are you sure "
                        + activity.javaClass.simpleName
                        + " is the currently displayed activity?"
                )
            }

            return Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                .apply {
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        activity.drawDecorViewToBitmap(this)
                    } else {
                        activity.runOnMainThreadSync {
                            activity.drawDecorViewToBitmap(this)
                        }
                    }
                }
        }

        private fun Activity.drawDecorViewToBitmap(bitmap: Bitmap) {
            val canvas = Canvas(bitmap)
            window.decorView.draw(canvas)
        }
    }
}
