package com.avito.android.runner.delegates

import com.avito.android.runner.InstrumentationDelegate
import com.avito.android.runner.InstrumentationDelegateProvider
import com.avito.android.runner.SystemDialogsManager
import com.avito.android.test.report.Report

class SystemDialogDelegate(
    private val report: Report
) : InstrumentationDelegate() {

    override fun beforeOnStart() {
        SystemDialogsManager(report).closeSystemDialogs()
    }

    class Provider : InstrumentationDelegateProvider {
        override fun get(context: InstrumentationDelegateProvider.Context): InstrumentationDelegate {
            return SystemDialogDelegate(context.report)
        }
    }
}