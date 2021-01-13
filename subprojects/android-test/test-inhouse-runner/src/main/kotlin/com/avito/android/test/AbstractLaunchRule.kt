package com.avito.android.test

import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.android.runner.checkPlayServices
import com.avito.android.test.util.getCurrentActivity
import com.avito.logger.create
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@Suppress("unused")
abstract class AbstractLaunchRule : TestRule {

    private val testContext = InstrumentationRegistry.getInstrumentation().context
    private val appUnderTestContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val loggerFactory by lazy {
        (InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner).loggerFactory
    }
    private val logger by lazy { loggerFactory.create<AbstractLaunchRule>() }

    override fun apply(base: Statement, description: Description): Statement = LaunchStatement(base)

    /**
     * Grants permissions needed to go through test scenarios without permission requests.
     */
    abstract fun grantPermissionsNeededForTesting()

    private inner class LaunchStatement(private val base: Statement) : Statement() {

        override fun evaluate() {
            try {
                beforeTest()
                base.evaluate()
            } catch (t: Throwable) {
                logger.critical("Error during test completion", t)
                throw t
            }
        }
    }

    fun startFromHomeScreen() {
        beforeAppStart()
        testContext.startActivity(Device.getLauncherIntentForAppUnderTest(testContext, appUnderTestContext))
        Device.waitForAppLaunchAndReady(appUnderTestContext)
    }

    fun startFromDeeplink(url: String) {
        beforeAppStart()
        testContext.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        Device.waitForAppLaunchAndReady(appUnderTestContext)
    }

    fun openDeeplink(url: String) {
        getCurrentActivity().startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        )
    }

    fun openDeeplinkWithAppContext(url: String) {
        appUnderTestContext.startActivity(
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
        appUnderTestContext.checkPlayServices(logger)
        grantPermissionsNeededForTesting()
    }
}
