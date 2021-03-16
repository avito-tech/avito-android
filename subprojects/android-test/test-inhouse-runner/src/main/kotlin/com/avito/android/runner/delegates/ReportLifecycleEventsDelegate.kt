package com.avito.android.runner.delegates

import android.os.Bundle
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import com.avito.android.runner.InstrumentationTestRunnerDelegate
import com.avito.android.test.report.Report
import com.avito.android.test.report.lifecycle.ReportActivityLifecycleListener

class ReportLifecycleEventsDelegate(
    private val report: Report
) : InstrumentationTestRunnerDelegate() {

    override fun afterOnCreate(arguments: Bundle) {
        ActivityLifecycleMonitorRegistry
            .getInstance()
            .addLifecycleCallback(ReportActivityLifecycleListener(report))
    }
}
