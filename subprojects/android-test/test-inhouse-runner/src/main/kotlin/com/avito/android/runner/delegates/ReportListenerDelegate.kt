package com.avito.android.runner.delegates

import android.os.Bundle
import com.avito.android.runner.InstrumentationDelegate
import com.avito.android.runner.InstrumentationDelegateProvider
import com.avito.android.runner.TestRunEnvironment
import com.avito.android.test.report.Report
import com.avito.android.test.report.ReportTestListener

class ReportListenerDelegate(
    private val report: Report,
    private val environment: TestRunEnvironment.RunEnvironment
) : InstrumentationDelegate() {

    override fun beforeOnCreate(arguments: Bundle) {
        arguments.putString("listener", ReportTestListener::class.java.name)
        arguments.putString("newRunListenerMode", "true")
        report.initTestCase(testMetadata = environment.testMetadata)
    }

    class Factory : InstrumentationDelegateProvider {
        override fun get(context: InstrumentationDelegateProvider.Context): InstrumentationDelegate {
            return ReportListenerDelegate(
                context.report,
                context.environment
            )
        }
    }
}