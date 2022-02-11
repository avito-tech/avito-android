package com.avito.android.test.report.screenshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import com.avito.android.Result
import com.avito.android.instrumentation.ActivityProvider
import com.avito.android.util.runOnMainThreadSync
import com.avito.report.TestArtifactsProvider
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap as AndroidBitmap

internal class ScreenshotCapturerImpl(
    private val testArtifactsProvider: TestArtifactsProvider,
    private val activityProvider: ActivityProvider,
    private val compressFormat: AndroidBitmap.CompressFormat = AndroidBitmap.CompressFormat.PNG,
    private val quality: Int = 100,
) : ScreenshotCapturer {

    /**
     * @return null when no Activity in RESUMED state
     */
    override fun captureAsFile(
        filename: String
    ): Result<File> {
        return testArtifactsProvider.provideReportDir().flatMap { dir ->
            captureBitmap().map { capture ->
                File(dir, filename).also { file ->
                    FileOutputStream(file).use {
                        capture.compress(compressFormat, quality, it)
                    }
                }
            }
        }
    }

    private fun captureBitmap(): Result<AndroidBitmap> {
        return activityProvider.getCurrentActivity().map { activity ->
            drawCanvas(activity)
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

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        activity.runOnMainThreadSync {
            activity.drawDecorViewToBitmap(bitmap)
        }
        return bitmap
    }

    private fun Activity.drawDecorViewToBitmap(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        window.decorView.draw(canvas)
    }
}
