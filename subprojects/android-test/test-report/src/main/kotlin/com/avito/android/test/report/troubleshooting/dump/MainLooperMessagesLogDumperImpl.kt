package com.avito.android.test.report.troubleshooting.dump

import android.os.Looper
import android.util.Printer
import com.avito.time.TimeProvider

class MainLooperMessagesLogDumperImpl(
    private val timeProvider: TimeProvider
) : Printer, MainLooperMessagesLogDumper {

    private val logs = StringBuilder()

    override fun println(log: String) {
        val message = sanitizeMessage(log)
        logs.appendLine("[${timeProvider.nowInMillis()}]: $message")
    }

    private fun sanitizeMessage(message: String): String {
        if (message.startsWith(START_PREFIX)) {
            return message.replace(START_PREFIX, ">", ignoreCase = true)
        }
        if (message.startsWith(FINISH_PREFIX)) {
            return message.replace(FINISH_PREFIX, "<", ignoreCase = true)
        }
        return message
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

private const val START_PREFIX = ">>>>> Dispatching to Handler"
private const val FINISH_PREFIX = "<<<<< Finished to Handler"
