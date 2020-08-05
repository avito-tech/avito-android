package com.avito.android.test.screenshot_test.internal

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import kotlin.math.roundToInt

internal data class DeviceDirectoryName(
    private val sdkVersion: String,
    private val resolution: String,
    private val density: String
) {

    val name = "${sdkVersion}_${resolution}_${density}"

    companion object {
        fun create(context: Context) = DeviceDirectoryName(
            sdkVersion = getSdkVersion(),
            resolution = context.getResolution(),
            density = context.getDensity()
        )

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
            return "API_"+android.os.Build.VERSION.SDK_INT.toString()
        }
    }
}

