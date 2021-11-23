package com.avito.android.build_verdict.span

import java.net.URL

public data class SpannedString(
    val plain: String,
    val html: String
) {

    public constructor(value: String) : this(value, value)

    public fun isEmpty(): Boolean {
        return plain.isEmpty() && html.isEmpty()
    }

    public companion object {

        @JvmStatic
        public fun String.toSpanned(): SpannedString = SpannedString(this)

        @JvmStatic
        public fun link(url: String, text: String): SpannedString {
            return link(URL(url), text)
        }

        @JvmStatic
        public fun link(url: URL, text: String): SpannedString {
            return SpannedString(
                plain = "$text: $url",
                html = """<a href="$url" target="_blank">$text</a>"""
            )
        }

        @JvmStatic
        public fun multiline(lines: List<SpannedString>): SpannedString {
            return SpannedStringBuilder()
                .addLines(lines)
                .build()
        }
    }
}
