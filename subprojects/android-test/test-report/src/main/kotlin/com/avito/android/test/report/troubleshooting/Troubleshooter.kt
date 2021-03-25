package com.avito.android.test.report.troubleshooting

import com.avito.android.test.report.Report
import com.avito.android.test.report.troubleshooting.dump.MainLooperMessagesLogDumper
import com.avito.android.test.report.troubleshooting.dump.MainLooperQueueDumper
import com.avito.android.test.report.troubleshooting.dump.ThreadDumper
import com.avito.android.test.report.troubleshooting.dump.ViewHierarchyDumper

interface Troubleshooter {

    fun troubleshootTo(report: Report)

    class Impl(
        private val mainLooperMessagesLogDumper: MainLooperMessagesLogDumper
    ) : Troubleshooter {

        override fun troubleshootTo(report: Report) {
            with(report) {
                addText("Threads dump", ThreadDumper.getThreadDump())
                addText("Main looper processed messages dump", mainLooperMessagesLogDumper.getMessagesLogDump())
                addText("Main looper queue dump", MainLooperQueueDumper.getDump())
                addText("View hierarchy dump", ViewHierarchyDumper.getDump())
            }
        }
    }
}
