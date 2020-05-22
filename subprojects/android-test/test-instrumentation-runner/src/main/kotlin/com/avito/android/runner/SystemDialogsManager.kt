package com.avito.android.runner

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector

/**
 * Based on https://github.com/nenick/espresso-macchiato
 */
class SystemDialogsManager(private val errorsReporter: ErrorsReporter) {

    @SuppressLint("LogNotTimber")
    fun closeSystemDialogs() {
        try {
            dismissCrashDialogIfShown()
        } catch (t: Throwable) {
            Log.v(TAG, "Failed to close crash dialog: ${t.message}")
            errorsReporter.reportError(t)
        }

        try {
            dismissAnrDialogIfShown()
        } catch (t: Throwable) {
            Log.v(TAG, "Failed to close application not respond dialog: ${t.message}")
            errorsReporter.reportError(t)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                dismissHiddenApiDialog()
            } catch (t: Throwable) {
                Log.v(TAG, "Failed to close hidden api dialog: ${t.message}")
                errorsReporter.reportError(t)
            }
        }
    }

    private fun dismissCrashDialogIfShown() {
        val crashStrings = listOfNotNull(
            stringResourceByName("aerr_application", ".*"),
            stringResourceByName("aerr_application_repeated", ".*"),
            stringResourceByName("aerr_process", ".*"),
            stringResourceByName("aerr_process_repeated", ".*")
        )
            .joinToOrRegexp()

        if (crashStrings.isEmpty()) {
            throw RuntimeException("Unable to find any of resources in crashStrings")
        }

        if (elementWithTextExists(crashStrings)) {
            val ok = stringResourceByName("ok")
            val close = stringResourceByName("aerr_close")
            val closeApp = stringResourceByName("aerr_close_app")

            when {
                ok != null && elementWithTextExists(ok) -> click(ok)
                close != null && elementWithTextExists(close) -> click(close)
                closeApp != null && elementWithTextExists(closeApp) -> click(closeApp)
                else -> throw IllegalStateException("Found crash dialog but can't find dismiss button")
            }
        }
    }

    private fun dismissAnrDialogIfShown() {
        val anrStrings = listOfNotNull(
            stringResourceByName("anr_process", ".*"),
            stringResourceByName("anr_activity_application", ".*", ".*"),
            stringResourceByName("anr_application_process", ".*"),
            stringResourceByName("anr_activity_process", ".*")
        ).joinToOrRegexp()

        if (anrStrings.isEmpty()) {
            throw RuntimeException("Unable to find any of resources in anrStrings")
        }

        if (elementWithTextExists(anrStrings)) {
            val wait = stringResourceByName("wait") ?: throw RuntimeException(
                "Found anr dialog but can't find wait button"
            )

            click(wait)
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi", "LogNotTimber")
    private fun dismissHiddenApiDialog() {
        val aClass = Class.forName("android.content.pm.PackageParser\$Package")
        val declaredConstructor = aClass.getDeclaredConstructor(String::class.java)
        declaredConstructor.isAccessible = true

        val cls = Class.forName("android.app.ActivityThread")
        val declaredMethod = cls.getDeclaredMethod("currentActivityThread")
        declaredMethod.isAccessible = true
        val activityThread = declaredMethod.invoke(null)
        val mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown")
        mHiddenApiWarningShown.isAccessible = true
        mHiddenApiWarningShown.setBoolean(activityThread, true)
    }

    private fun elementWithTextExists(expectedMessage: String): Boolean {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val dialog = device.findObject(UiSelector().textMatches(expectedMessage))
        return dialog.exists()
    }

    private fun click(textSelector: String) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val targetUiObject = device.findObject(UiSelector().text(textSelector))
        targetUiObject.click()
    }

    private fun stringResourceByName(name: String, vararg formatArgs: String): String? = try {
        // for all available strings see Android/sdk/platforms/android-28/data/res/values/strings.xml
        val resId =
            InstrumentationRegistry.getInstrumentation().context.resources.getIdentifier(name, "string", "android")
        InstrumentationRegistry.getInstrumentation().context.getString(resId, *formatArgs)
    } catch (t: Throwable) {
        null
    }

    private fun List<String>.joinToOrRegexp(): String =
        joinToString(separator = "|") {
            it.replace("?", "\\?")
        }.let {
            "($it)"
        }
}

private const val TAG = "SystemDialogsManager"
