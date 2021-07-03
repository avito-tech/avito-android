package com.avito.slack

import okhttp3.HttpUrl

public object SlackStringFormat {

    public const val mentionChannel: String = "<!channel>"

    public fun ellipsize(string: String, limit: Int): String {
        return if (string.length <= limit) {
            string
        } else {
            string.take(limit) + "<...>"
        }
    }

    public fun link(label: String, url: String): String = "<$url|$label>"

    public fun link(label: String, url: HttpUrl): String = "<$url|$label>"
}
