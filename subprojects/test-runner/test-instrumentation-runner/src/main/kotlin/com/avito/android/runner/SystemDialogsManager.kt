package com.avito.android.runner

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.avito.logger.LoggerFactory
import com.avito.logger.create

/**
 * Based on https://github.com/nenick/espresso-macchiato
 */
internal class SystemDialogsManager(loggerFactory: LoggerFactory) {

    private val logger = loggerFactory.create<Settings.System>()

    private val waysToDismissDialog: List<(UiDevice) -> Unit> = listOf(
        { uiDevice ->
            uiDevice.wait(
                Until.findObject(By.res("android:id/button1")),
                DISMISS_DIALOG_TIMEOUT
            ).click()
        },
        { uiDevice ->
            uiDevice.wait(
                Until.findObject(By.res("android:id/closeButton")),
                DISMISS_DIALOG_TIMEOUT
            ).click()
        },
        { uiDevice ->
            uiDevice.wait(
                Until.findObject(By.res("com.android.internal:id/aerr_close")),
                DISMISS_DIALOG_TIMEOUT
            ).click()
        },
        { uiDevice ->
            uiDevice.wait(
                Until.findObject(By.res("com.android.packageinstaller:id/permission_deny_button")),
                DISMISS_DIALOG_TIMEOUT
            ).click()
        },
        { uiDevice ->
            uiDevice.wait(
                Until.findObject(By.res("com.android.permissioncontroller:id/permission_deny_button")),
                DISMISS_DIALOG_TIMEOUT
            ).click()
        },
        { uiDevice -> uiDevice.pressBack() }
    )

    fun closeSystemDialogs() {
        try {
            dismissCrashDialogIfShown()
        } catch (t: Throwable) {
            logger.warn("Failed to close crash dialog", t)
        }

        try {
            dismissAnrDialogIfShown()
        } catch (t: Throwable) {
            logger.warn("Failed to close application not respond dialog", t)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                dismissHiddenApiDialog()
            } catch (t: Throwable) {
                logger.warn("Failed to close hidden api dialog", t)
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
            tryToDismissSystemDialog()

            if (elementWithTextExists(crashStrings)) {
                throw IllegalStateException("Found crash dialog but failed to dismiss it")
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

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
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

    private fun tryToDismissSystemDialog() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        waysToDismissDialog.forEach { attemptToDismiss ->
            try {
                attemptToDismiss.invoke(uiDevice)
            } catch (error: Throwable) {
                // ignore
            }
        }
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

    companion object {
        private const val DISMISS_DIALOG_TIMEOUT: Long = 1000
    }
}
