package com.avito.android.test.report.troubleshooting.dump

public class ThreadDumper : Dumper {

    override val label: String = "Threads dump"

    override fun dump(): String {
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
