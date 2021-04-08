package com.avito.android.build_verdict.span

class SpannedStringBuilder {
    private val lines: MutableList<SpannedString>

    constructor() {
        lines = mutableListOf()
    }

    constructor(initialLine: SpannedString) {
        lines = mutableListOf(initialLine)
    }

    fun addLine(line: SpannedString): SpannedStringBuilder {
        lines.add(line)
        return this
    }

    fun addLines(newLines: Collection<SpannedString>): SpannedStringBuilder {
        lines.addAll(newLines)
        return this
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
