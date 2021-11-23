package com.avito.android.build_verdict.span

public class SpannedStringBuilder {

    private val lines: MutableList<SpannedString>

    public constructor() {
        lines = mutableListOf()
    }

    public constructor(initialLine: SpannedString) {
        lines = mutableListOf(initialLine)
    }

    public fun addLine(line: SpannedString): SpannedStringBuilder {
        lines.add(line)
        return this
    }

    public fun addLines(newLines: Collection<SpannedString>): SpannedStringBuilder {
        lines.addAll(newLines)
        return this
    }

    public fun build(): SpannedString {
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
