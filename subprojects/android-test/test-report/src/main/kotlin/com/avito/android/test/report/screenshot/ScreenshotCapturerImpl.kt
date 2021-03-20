package com.avito.android.test.report.screenshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import com.avito.android.Result
import com.avito.android.test.util.getCurrentActivityOrNull
import com.avito.android.util.runOnMainThreadSync
import com.avito.android.util.toPng
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Source: https://github.com/square/spoon/blob/master/spoon-client/src/main/java/com/squareup/spoon/Screenshot.java
 */
class ScreenshotCapturerImpl(private val outputDirectory: Lazy<File>) : ScreenshotCapturer {

    override fun captureBitmap(): Result<Bitmap> {
        // todo use di: pass activity getter as constructor argument
        val activity = getCurrentActivityOrNull()
        return if (activity != null) {
            try {
                Result.Success(drawCanvas(activity))
            } catch (e: Throwable) {
                Result.Failure(IllegalStateException("Can't make screenshot, drawCanvas() exception", e))
            }
        } else {
            Result.Failure(IllegalStateException("There is no RESUMED activity when capturingBitmap"))
        }
    }

    override fun captureAsStream(): Result<InputStream> {
        return captureBitmap().map { bitmap ->
            bitmap.toPng()
        }
    }

    override fun captureAsFile(
        filename: String,
        compressFormat: Bitmap.CompressFormat,
        quality: Int
    ): Result<File> {
        return captureBitmap().map { bitmap ->
            File(outputDirectory.value, filename).also { file ->
                FileOutputStream(file).use {
                    bitmap.compress(compressFormat, quality, it)
                }
            }
        }
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
