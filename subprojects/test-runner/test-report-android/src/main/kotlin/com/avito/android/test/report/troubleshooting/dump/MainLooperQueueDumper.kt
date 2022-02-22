package com.avito.android.test.report.troubleshooting.dump

import android.os.Looper

class MainLooperQueueDumper : Dumper {

    override val label: String = "Main looper queue dump"

    override fun dump(): String {
        val dump = StringBuilder()
        Looper.getMainLooper().dump(
            { line ->
                dump.appendLine(line)
            },
            "" // empty prefix
        )
        return dump.toString()
    }
}
