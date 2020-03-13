package com.avito.android.runner.delegates

import androidx.test.espresso.Espresso
import androidx.test.espresso.FailureHandler
import com.avito.android.runner.InstrumentationDelegate
import com.avito.android.runner.InstrumentationDelegateProvider
import com.avito.android.test.UITestConfig
import com.avito.android.test.interceptor.HumanReadableActionInterceptor
import com.avito.android.test.interceptor.HumanReadableAssertionInterceptor
import com.avito.android.test.report.Report
import com.avito.android.util.DeviceSettingsChecker
import java.util.concurrent.TimeUnit

class UiTestConfigDelegate(
    private val failureHandler: FailureHandler,
    private val report: Report,
    private val deviceSettingsChecker: DeviceSettingsChecker
) : InstrumentationDelegate() {

    override fun afterOnCreate() {
        Espresso.setFailureHandler(failureHandler)
        initUITestConfig()
        deviceSettingsChecker.check()
    }

    private fun initUITestConfig() {
        with(UITestConfig) {
            waiterTimeoutMs = TimeUnit.SECONDS.toMillis(12)

            activityLaunchTimeoutMilliseconds = TimeUnit.SECONDS.toMillis(15)

            actionInterceptors += HumanReadableActionInterceptor {
                report.addComment(it)
            }

            assertionInterceptors += HumanReadableAssertionInterceptor {
                report.addComment(it)
            }

            onWaiterRetry = { }
        }
    }

    class Provider : InstrumentationDelegateProvider {
        override fun get(context: InstrumentationDelegateProvider.Context): InstrumentationDelegate {
            return UiTestConfigDelegate(
                failureHandler = context.friendlyErrorHandler,
                report = context.report,
                deviceSettingsChecker = context.deviceSettingsChecker
            )
        }
    }
}