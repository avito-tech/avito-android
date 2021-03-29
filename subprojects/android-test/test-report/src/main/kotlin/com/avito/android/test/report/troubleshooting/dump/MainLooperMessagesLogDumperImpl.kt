package com.avito.android.test.report.troubleshooting.dump

import android.os.Looper
import android.util.Printer

class MainLooperMessagesLogDumperImpl : Printer, MainLooperMessagesLogDumper {

    private val logs = StringBuilder()

    override fun println(log: String) {
        logs.appendLine(log)
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
