package com.avito.alertino

import okhttp3.HttpUrl

public object AlertinoStringFormat {

    public const val mentionChannel: String = "@channel"

    public fun ellipsize(string: String, limit: Int): String {
        return if (string.length <= limit) {
            string
        } else {
            string.take(limit) + "<...>"
        }
    }

    public fun link(label: String, url: String): String = "[$label]($url)"

    public fun link(label: String, url: HttpUrl): String = "[$label]($url)"
}
