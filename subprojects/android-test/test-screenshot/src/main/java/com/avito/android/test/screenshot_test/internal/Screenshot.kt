package com.avito.android.test.screenshot_test.internal

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import java.io.File
import kotlin.math.roundToInt

class Screenshot(
    emulatorSpecification: String,
    name: String,
    extension: String,
    dir: File
) {
    val path: File = File(dir, "$name.$extension")
    val emulatorSpecificPath = "$emulatorSpecification/$name.$extension"
}

class ScreenshotDirectory(
    val emulatorSpecification: String,
    val path: File
) {

    fun getScreenshot(name: String, extension: String = "png"): Screenshot {
        return Screenshot(
            emulatorSpecification = emulatorSpecification,
            name = name,
            extension = extension,
            dir = path
        )
    }

    companion object {

        fun create(
            context: Context,
            prefix: String
        ): ScreenshotDirectory {
            val externalStorage = requireNotNull(System.getenv("EXTERNAL_STORAGE")) {
                "System.env EXTERNAL_STORAGE is null"
            }
            val parent = "$externalStorage/$prefix/${context.packageName}.test/"
            val rootScreenshotDir = File(parent)
            if (!rootScreenshotDir.exists()) {
                rootScreenshotDir.mkdirs()
            }

            val emulatorSpecification = "${getSdkVersion()}_${context.getResolution()}_${context.getDensity()}"
            val path = File(rootScreenshotDir, emulatorSpecification)
            if (!path.exists()) {
                require(path.mkdir()) {
                    "Failed to create the directory ${path.absolutePath} for screenshots."
                }
            }
            setWorldWriteable(path)
            return ScreenshotDirectory(
                emulatorSpecification = emulatorSpecification,
                path = path
            )
        }

        @SuppressLint("SetWorldWritable")
        private fun setWorldWriteable(dir: File) {
            dir.setWritable( /* writeable = */true,  /* ownerOnly = */false)
        }

        private fun Context.getResolution(): String {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = wm.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            return "${width}x$height"
        }

        private fun Context.getDensity(): String {
            return (resources.displayMetrics.density * 160).roundToInt().toString()
        }

        private fun getSdkVersion(): String {
            return "API_" + android.os.Build.VERSION.SDK_INT.toString()
        }
    }
}
