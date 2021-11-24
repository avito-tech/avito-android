package com.avito.android.build_verdict.internal

internal class LogsTextBuilder {

    private val lines: MutableList<String>

    constructor() {
        lines = mutableListOf()
    }

    constructor(initialLine: String) {
        lines = mutableListOf(initialLine)
    }

    fun addLine(line: String) {
        lines.add(line)
    }

    fun build(): String {
        return lines.joinToString(
            separator = "\n"
        ) { line -> line.removeSuffix("\n") }
    }
}
