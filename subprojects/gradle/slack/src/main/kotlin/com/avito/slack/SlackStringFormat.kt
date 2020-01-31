package com.avito.slack

import okhttp3.HttpUrl

object SlackStringFormat {

    const val mentionChannel = "<!channel>"

    fun ellipsize(string: String, limit: Int): String {
        return if (string.length <= limit) {
            string
        } else {
            string.take(limit) + "<...>"
        }
    }

    fun link(label: String, url: String): String = "<$url|$label>"

    fun link(label: String, url: HttpUrl): String = "<$url|$label>"

}
