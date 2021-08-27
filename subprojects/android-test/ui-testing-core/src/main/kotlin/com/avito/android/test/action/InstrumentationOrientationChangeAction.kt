package com.avito.android.test.action

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.util.getCurrentActivity

internal class InstrumentationOrientationChangeAction {

    fun rotate() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.runOnMainSync {
            with(getCurrentActivity()) {
                requestedOrientation = decideOrientationToToggle(this)
            }
        }
        // rotation is async
        instrumentation.waitForIdleSync()
    }

    private fun decideOrientationToToggle(activity: Activity): Int =
        when (val currentOrientation = activity.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else -> throw IllegalStateException("Unsupported orientation: $currentOrientation")
        }
}
