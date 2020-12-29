package com.avito.android.build_verdict.internal.span

import com.avito.android.build_verdict.span.SpannedString

class SpannedStringBuilder {
    private val lines: MutableList<SpannedString>

    constructor() {
        lines = mutableListOf()
    }

    constructor(initialLine: SpannedString) {
        lines = mutableListOf(initialLine)
    }

    fun addLine(line: SpannedString) {
        lines.add(line)
    }

    fun build(): SpannedString {
        val plain = lines.joinToString(
            separator = "\n"
        ) { line -> line.plain.removeSuffix("\n") }
        val html = lines.joinToString(
            separator = "\n"
        ) { line -> line.html.removeSuffix("\n") }

        return SpannedString(
            plain = plain,
            html = html
        )
    }
}
