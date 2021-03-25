package com.avito.android.test.report.troubleshooting.dump

import android.os.Looper
import android.util.Printer

class MainLooperMessagesLogDumper : Printer {

    private val logs = StringBuilder()

    init {
        Looper.getMainLooper().setMessageLogging(this)
    }

    override fun println(log: String) {
        logs.appendLine(log)
    }

    fun getMessagesLogDump(): String {
        return logs.toString()
    }

    fun start() {
        logs.clear() // if we run a few tests with one instrumentation instance
        Looper.getMainLooper().setMessageLogging(this)
    }

    fun stop() {
        Looper.getMainLooper().setMessageLogging(null)
    }
}
