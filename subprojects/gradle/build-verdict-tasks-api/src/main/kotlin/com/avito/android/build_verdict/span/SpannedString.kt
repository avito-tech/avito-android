package com.avito.android.build_verdict.span

import java.net.URL

data class SpannedString(
    val plain: String,
    val html: String
) {
    constructor(value: String) : this(value, value)

    companion object {

        @JvmStatic
        fun String.toSpanned(): SpannedString = SpannedString(this)

        @JvmStatic
        fun link(url: String, text: String): SpannedString {
            return link(URL(url), text)
        }

        @JvmStatic
        fun link(url: URL, text: String): SpannedString {
            return SpannedString(
                plain = "$text: $url",
                html = """<a href="$url" target="_blank">$text</a>"""
            )
        }
    }
}
