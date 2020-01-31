package com.avito.http

import okhttp3.RequestBody
import okio.Buffer
import java.io.EOFException
import java.nio.charset.Charset

private val utf8 = Charset.forName("UTF-8")

fun RequestBody.toPlainText(): String? {
    val buffer = Buffer()
    writeTo(buffer)

    var charset: Charset? = utf8

    val contentType = contentType()
    if (contentType != null) {
        charset = contentType.charset(utf8)
    }

    return if (isPlaintext(buffer)) {
        buffer.readString(charset!!)
    } else {
        null
    }
}

/**
 * Returns true if the body probably contains human readable text. Uses a small sample
 * of code points to detect unicode control characters commonly used in binary file signatures.
 */
fun isPlaintext(buffer: Buffer): Boolean {
    try {
        val prefix = Buffer()
        val byteCount = if (buffer.size() < 64) buffer.size() else 64
        buffer.copyTo(prefix, 0, byteCount)
        for (i in 0..15) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                return false
            }
        }
        return true
    } catch (e: EOFException) {
        return false // Truncated UTF-8 sequence.
    }
}
