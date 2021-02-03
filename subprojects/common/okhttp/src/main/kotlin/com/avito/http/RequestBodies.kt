package com.avito.http

import com.avito.http.internal.isPlaintext
import okhttp3.RequestBody
import okio.Buffer
import java.nio.charset.Charset

private val utf8 = Charset.forName("UTF-8")

public fun RequestBody.toPlainText(): String? {
    val buffer = Buffer()
    writeTo(buffer)

    var charset: Charset? = utf8

    val contentType = contentType()
    if (contentType != null) {
        charset = contentType.charset(utf8)
    }

    return if (buffer.isPlaintext()) {
        buffer.readString(charset!!)
    } else {
        null
    }
}
