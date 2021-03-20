package com.avito.android.test.report.troubleshooting.dump

import android.os.Looper

object MainLooperDumper {

    fun getDump(): String {
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
