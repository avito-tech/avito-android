package com.avito.android.test.report.screenshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import com.avito.android.test.util.getCurrentActivityOrNull
import com.avito.android.util.runOnMainThreadSync
import com.avito.android.util.toPng
import com.avito.logger.Logger
import org.funktionale.option.Option
import org.funktionale.tries.Try
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

interface ScreenshotCapturer {

    /**
     * @return Result is absent when there is no RESUMED Activity for screenshooting
     */
    fun captureBitmap(): Try<Option<Bitmap>>

    /**
     * @return Result is absent when there is no RESUMED Activity for screenshooting
     */
    fun captureAsStream(): Try<Option<InputStream>>

    /**
     * @return Result is absent when there is no RESUMED Activity for screenshooting
     */
    fun captureAsFile(
        filename: String = "${UUID.randomUUID()}.png",
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Try<Option<File>>

    /**
     * Source: https://github.com/square/spoon/blob/master/spoon-client/src/main/java/com/squareup/spoon/Screenshot.java
     */
    class Impl(
        private val outputDirectory: Lazy<File>,
        private val logger: Logger
    ) : ScreenshotCapturer {

        override fun captureBitmap(): Try<Option<Bitmap>> {
            //todo use di: pass activity getter as constructor argument
            val activity = getCurrentActivityOrNull()
            return if (activity != null) {
                Try {
                    Option.Some(drawCanvas(activity))
                }
            } else {
                logger.warn("There is no RESUMED activity when capturingBitmap")
                Try {
                    Option.empty<Bitmap>()
                }
            }
        }

        override fun captureAsStream(): Try<Option<InputStream>> {
            return captureBitmap().map {
                it.map { bitmap -> bitmap.toPng() }
            }
        }

        override fun captureAsFile(
            filename: String,
            compressFormat: Bitmap.CompressFormat,
            quality: Int
        ): Try<Option<File>> {
            return captureBitmap().map {
                it.map { bitmap ->
                    File(outputDirectory.value, filename).also { file ->
                        FileOutputStream(file).use {
                            bitmap.compress(compressFormat, quality, it)
                        }
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
}
