package com.avito.android.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.ViewConfiguration
import com.avito.logger.LoggerFactory
import com.avito.logger.create

class DeviceSettingsChecker(
    private val context: Context,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.create<DeviceSettingsChecker>()

    fun check() {
        checkSupportedApi()

        val warnings: List<String> = listOfNotNull(
            checkWindowAnimationScale(),
            checkTransitionAnimationScale(),
            checkAnimatorDurationScale(),
            checkLongPressDuration()
        )
        if (warnings.isNotEmpty()) {
            // TODO: Try to fix them of fail MBS-7355
            logger.warn(
                "=== ERROR=== \n" +
                    "Emulator has incorrect settings which cause flakiness:\n" +
                    warnings.joinToString(separator = "") { "- $it\n" } +
                    "\nSee https://avito-tech.github.io/avito-android/docs/ci/containers/#android-emulator-images"
            )
        }
    }

    private fun checkSupportedApi() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            throw IllegalStateException("Android 12 (API level 31) and above is not supported yet")
        }
    }

    private fun checkWindowAnimationScale(): String? {
        return if (Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f) > 0) {
            "Window animation scale must be turned off"
        } else {
            null
        }
    }

    private fun checkTransitionAnimationScale(): String? {
        return if (
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            ) > 0
        ) {
            "Transition animation scale must be turned off"
        } else {
            null
        }
    }

    private fun checkAnimatorDurationScale(): String? {
        return if (Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0) {
            "Animator animation scale must be turned off"
        } else {
            null
        }
    }

    private fun checkLongPressDuration(): String? {
        return if (ViewConfiguration.getLongPressTimeout() < RECOMMENDED_LONG_PRESS_DURATION) {
            "Long press duration must be at least 1500 ms"
        } else {
            null
        }
    }
}

/**
 * default android 500 leads to flaky press/long-press detection
 */
private const val RECOMMENDED_LONG_PRESS_DURATION = 1500
