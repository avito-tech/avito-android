package com.avito.android.test.report.troubleshooting.dump

object NoOpMainLooper : MainLooperMessagesLogDumper {

    override fun getMessagesLogDump() = "no-op"

    override fun start() {
        // no-op
    }

    override fun stop() {
        // no-op
    }
}
