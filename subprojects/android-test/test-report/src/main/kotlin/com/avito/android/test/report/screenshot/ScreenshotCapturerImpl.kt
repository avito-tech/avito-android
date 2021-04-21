package com.avito.android.test.report.screenshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import com.avito.android.Result
import com.avito.android.instrumentation.ActivityProvider
import com.avito.android.test.report.screenshot.ScreenshotCapturer.Capture
import com.avito.android.util.runOnMainThreadSync
import com.avito.report.TestArtifactsProvider
import java.io.File
import java.io.FileOutputStream

internal class ScreenshotCapturerImpl(
    private val testArtifactsProvider: TestArtifactsProvider,
    private val activityProvider: ActivityProvider
) : ScreenshotCapturer {

    override fun captureBitmap(): Result<Capture> {
        val activity = activityProvider.getCurrentActivity()
        return if (activity != null) {
            try {
                Result.Success(Capture.Bitmap(drawCanvas(activity)))
            } catch (e: Throwable) {
                Result.Failure(IllegalStateException("Can't make screenshot, drawCanvas() exception", e))
            }
        } else {
            Result.Success(Capture.NoActivity)
        }
    }

    override fun captureAsFile(
        filename: String,
        compressFormat: Bitmap.CompressFormat,
        quality: Int
    ): Result<File?> {
        return testArtifactsProvider.provideReportDir()
            .flatMap { dir ->
                captureBitmap().flatMap { capture ->
                    Result.Success(
                        when (capture) {
                            is Capture.Bitmap ->
                                File(dir, filename).also { file ->
                                    FileOutputStream(file).use {
                                        capture.value.compress(compressFormat, quality, it)
                                    }
                                }

                            Capture.NoActivity -> null // no Activity in RESUMED
                        }
                    )
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

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        if (Looper.myLooper() == Looper.getMainLooper()) {
            activity.drawDecorViewToBitmap(bitmap)
        } else {
            activity.runOnMainThreadSync {
                activity.drawDecorViewToBitmap(bitmap)
            }
        }
        return bitmap
    }

    private fun Activity.drawDecorViewToBitmap(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        window.decorView.draw(canvas)
    }
}
