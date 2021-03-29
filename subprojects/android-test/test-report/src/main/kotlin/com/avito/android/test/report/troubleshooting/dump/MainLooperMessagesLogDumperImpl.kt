package com.avito.android.test.report.troubleshooting.dump

import android.os.Looper
import android.util.Printer
import com.avito.time.TimeProvider

class MainLooperMessagesLogDumperImpl(
    private val timeProvider: TimeProvider
) : Printer, MainLooperMessagesLogDumper {

    private val logs = StringBuilder()

    override fun println(log: String) {
        logs.appendLine("[${timeProvider.nowInMillis()}]: $log")
    }

    override fun getMessagesLogDump(): String {
        return logs.toString()
    }

    override fun start() {
        logs.clear() // if we run a few tests with one instrumentation instance
        Looper.getMainLooper().setMessageLogging(this)
    }

    override fun stop() {
        Looper.getMainLooper().setMessageLogging(null)
    }
}
