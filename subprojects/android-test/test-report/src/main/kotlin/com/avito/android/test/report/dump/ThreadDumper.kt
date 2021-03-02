package com.avito.android.test.report.dump

object ThreadDumper {

    fun getThreadDump(): String {
        return buildString {
            Thread.getAllStackTraces().forEach { (thread, stackTrace) ->
                appendLine("$thread, ${thread.state}")
                stackTrace.forEach { stackElement ->
                    append("\t")
                    appendLine(stackElement.toString())
                }
                appendLine()
            }
        }
    }
}
