package com.avito.android.test.report.troubleshooting

import com.avito.android.test.report.Report
import com.avito.android.test.report.troubleshooting.dump.MainLooperDumper
import com.avito.android.test.report.troubleshooting.dump.ThreadDumper

interface Troubleshooter {

    fun troubleshootTo(report: Report)

    object Impl : Troubleshooter {

        override fun troubleshootTo(report: Report) {
            with(report) {
                addText("Threads dump", ThreadDumper.getThreadDump())
                addText("Main looper dump", MainLooperDumper.getDump())
            }
        }
    }
}
