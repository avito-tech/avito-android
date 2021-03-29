package com.avito.android.test.report.troubleshooting.dump

interface MainLooperMessagesLogDumper {
    fun getMessagesLogDump(): String
    fun start()
    fun stop()
}
