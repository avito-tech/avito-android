package com.avito.android.runner.delegates

import android.os.Bundle
import com.avito.android.runner.InstrumentationTestRunnerDelegate
import com.avito.android.test.report.troubleshooting.dump.MainLooperMessagesLogDumper

class MainLooperMessagesLogDelegate(
    private val dumper: MainLooperMessagesLogDumper
) : InstrumentationTestRunnerDelegate() {

    override fun beforeOnStart() {
        dumper.start()
    }

    override fun beforeFinish(resultCode: Int, results: Bundle?) {
        dumper.stop()
    }
}
