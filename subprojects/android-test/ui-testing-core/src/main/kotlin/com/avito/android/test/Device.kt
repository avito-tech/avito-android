package com.avito.android.test

import android.Manifest.permission.QUERY_ALL_PACKAGES
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.permission.PermissionRequester
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.avito.android.test.action.InstrumentationOrientationChangeAction
import com.avito.android.test.espresso.action.OrientationChangeAction
import com.avito.android.test.internal.Cache
import com.avito.android.test.internal.SQLiteDB
import com.avito.android.test.internal.SharedPreferences
import com.avito.android.test.page_object.KeyboardElement
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Abstraction of android phone from user's perspective
 * Contains actions and checks not associated with apps
 */
object Device {

    private val uiDevice by lazy { UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()) }

    val keyboard = KeyboardElement()

    /**
     * Changes device orientation between portrait and landscape
     */
    fun rotate() {
        if (Build.VERSION.SDK_INT >= 30) {
            // ViewAction with default root matcher is flaky
            // It can pick wrong window in case of opened dialog
            InstrumentationOrientationChangeAction().rotate()
        } else {
            onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.toggle())
        }
    }

    /**
     * short press on back button, ignore application under test boundaries
     */
    fun pressBack(failTestIfAppUnderTestClosed: Boolean = false) {
        if (failTestIfAppUnderTestClosed) {
            Espresso.pressBack()
        } else {
            Espresso.pressBackUnconditionally()
        }
    }

    /**
     * short press on home button
     */
    fun pressHome() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressHome()
    }

    /**
     * Waits for app's main thread to be idle
     * Used only in screenshot tests as a temp hack. Don't use it in component or functional tests
     * todo consider remove
     */
    fun waitForIdle() {
        onView(ViewMatchers.isRoot()).perform(OrientationChangeAction.toggle())
    }

    fun getLauncherIntentForAppUnderTest(appContext: Context): Intent {
        val launchIntent = requireNotNull(appContext.packageManager.getLaunchIntentForPackage(appContext.packageName))
        return launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    }

    fun waitForLauncher(timeout: Long = UITestConfig.activityLaunchTimeoutMilliseconds) {
        assertTrue(
            "Waiting for launcher screen was exceeded timeout: $timeout milliseconds",
            uiDevice.wait(Until.hasObject(getLauncherPackageNameSelector().depth(0)), timeout)
        )
    }

    private fun getLauncherPackageNameSelector(): BySelector {
        val noPackagesVisibilityRestriction = Build.VERSION.SDK_INT < 30 ||
            InstrumentationRegistry.getInstrumentation().targetContext.applicationInfo.targetSdkVersion < 30

        return if (noPackagesVisibilityRestriction || isPermissionGranted(QUERY_ALL_PACKAGES)) {
            val packageName = requireNotNull(uiDevice.launcherPackageName)
            return By.pkg(packageName)
        } else {
            // Due to package visibility restrictions:
            // https://developer.android.com/training/package-visibility/declaring
            // Other options are worse:
            // - <queries><intent> will change production behavior.
            //   Adding it only to some build variants will make others unusable for tests and more sophisticated configuration.
            //   Adding it through test manifest didn't work.
            // - Granted by default QUERY_ALL_PACKAGES permission will prevent testing package visibility restrictions.
            By.pkg(UITestConfig.deviceLauncherPackage)
        }
    }

    fun waitForAppLaunchAndReady(
        appContext: Context,
        timeout: Long = UITestConfig.activityLaunchTimeoutMilliseconds
    ) {
        with(uiDevice) {
            assertTrue(
                "Waiting for application launching was exceeded timeout: $timeout milliseconds",
                wait(Until.hasObject(By.pkg(appContext.packageName).depth(0)), timeout)
            )
        }
    }

    fun killApp(appContext: Context) {
        Runtime.getRuntime().exec(arrayOf("am", "force-stop", appContext.packageName))
    }

    fun grantPermissions(vararg permissions: String) {
        val requester = PermissionRequester()
        requester.addPermissions(*permissions)
        requester.requestPermissions()
    }

    private fun isPermissionGranted(permission: String): Boolean {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        return ContextCompat.checkSelfPermission(
            targetContext,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * WARNING: Currently not working correctly while app is running. Use only on app with no processes alive
     */
    fun clearApplicationData(appContext: Context = ApplicationProvider.getApplicationContext()) {
        Cache(appContext).clear()
        SQLiteDB(appContext).clearAll()
        SharedPreferences(appContext).clear()
    }

    object Push {

        fun openNotification(
            expectedTitle: String,
            timeoutMillis: Long = UITestConfig.openNotificationTimeoutMilliseconds
        ) {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.openNotification()
            assertTrue(
                "Waiting for notification with title: $expectedTitle was exceeded timeout: $timeoutMillis milliseconds",
                device.wait(Until.hasObject(By.text(expectedTitle)), timeoutMillis)
            )
            val titleObject = device.findObject(By.text(expectedTitle))
            assertEquals(
                "Notification has incorrect title",
                expectedTitle,
                titleObject.text
            )
            // it is possible to add a sleep here
            // to let some time to item to be synchronized with device after reject, message, etc
            titleObject.click()
        }

        fun receiveNotification(init: Notification.() -> Unit) {
            val notification = Notification()
            notification.init()
            val command = "am broadcast" +
                " -a ${notification.intent}" +
                " -n ${notification.packageName}/${notification.receiverName}" +
                " --es uri ${notification.uri}" +
                (notification.phash?.let { " --es phash $it" } ?: "") +
                " --es notification {\"body\":\"${notification.messageBody}\"}"
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .executeShellCommand(command)
        }

        class Notification {
            var intent: String = "com.google.android.c2dm.intent.RECEIVE"
            var packageName: String = ApplicationProvider.getApplicationContext<Application>().packageName
            var receiverName = "com.google.android.gms.gcm.GcmReceiver"
            var uri: String = ""
            var messageBody: String = ""
                set(value) {
                    assertThat(
                        "Value must not contains spaces",
                        value,
                        not(containsString(" "))
                    )
                    field = value
                }
            var phash: String? = null
        }
    }
}
