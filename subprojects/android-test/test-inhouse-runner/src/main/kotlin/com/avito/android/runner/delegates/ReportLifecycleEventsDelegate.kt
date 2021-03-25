package com.avito.android.runner.delegates

import android.os.Bundle
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import com.avito.android.runner.InstrumentationTestRunnerDelegate
import com.avito.android.test.report.Report
import com.avito.android.test.report.lifecycle.ReportActivityLifecycleListener

class ReportLifecycleEventsDelegate(
    report: Report
) : InstrumentationTestRunnerDelegate() {
    /**
     * ActivityLifecycleMonitorRegistry wraps callbacks by WeakReference.
     * So we need to hold reference else where for not be garbage collecting
     */
    private val listener = ReportActivityLifecycleListener(report)

    override fun afterOnCreate(arguments: Bundle) {
        ActivityLifecycleMonitorRegistry
            .getInstance()
            .addLifecycleCallback(listener)
    }

    override fun afterFinish(resultCode: Int, results: Bundle?) {
        ActivityLifecycleMonitorRegistry
            .getInstance()
            .removeLifecycleCallback(listener)
    }
}
