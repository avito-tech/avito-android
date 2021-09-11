package com.avito.android.test

import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.runner.checkPlayServices
import com.avito.android.test.util.getCurrentActivity
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@Suppress("unused")
abstract class AbstractLaunchRule : TestRule {

    private val instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    override fun apply(base: Statement, description: Description): Statement = LaunchStatement(base)

    /**
     * Grants permissions needed to go through test scenarios without permission requests.
     */
    abstract fun grantPermissionsNeededForTesting()

    private inner class LaunchStatement(private val base: Statement) : Statement() {

        override fun evaluate() {
            beforeTest()
            base.evaluate()
        }
    }

    fun startFromHomeScreen() {
        beforeAppStart()
        targetContext.startActivity(
            Device.getLauncherIntentForAppUnderTest(targetContext)
        )
        Device.waitForAppLaunchAndReady(targetContext)
    }

    fun startFromDeeplink(url: String) {
        beforeAppStart()
        targetContext.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        Device.waitForAppLaunchAndReady(targetContext)
    }

    fun openDeeplink(url: String) {
        getCurrentActivity().startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        )
    }

    fun openDeeplinkWithAppContext(url: String) {
        targetContext.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun beforeTest() {
        with(Device) {
            pressHome()
            waitForLauncher()
        }
    }

    private fun beforeAppStart() {
        targetContext.checkPlayServices()
        grantPermissionsNeededForTesting()
    }
}
