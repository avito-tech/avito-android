package com.avito.android.test.report

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.avito.android.test.UITestConfig
import com.avito.android.test.report.performance.PerformanceProvider
import com.avito.android.test.report.performance.PerformanceTestReporter
import com.avito.android.util.StartupTimeProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class PerformanceTestRule : TestRule {

    val startUp: StartupTimeProvider
        get() = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as StartupTimeProvider)

    val performanceReporter: PerformanceTestReporter
        get() = (InstrumentationRegistry.getInstrumentation() as PerformanceProvider).performanceTestReporter

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                UITestConfig.visualizeClicks = false
                UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                try {
                    base.evaluate()
                } finally {
                    UITestConfig.visualizeClicks = UITestConfig.defaultVisualizeClicks
                }
            }
        }
}
