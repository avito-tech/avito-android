package com.avito.i18n.plugin.xml

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.security.MessageDigest

internal const val RESOURCES_TAG = "resources"

internal fun String.hashSha1(): String {
    val digest = MessageDigest.getInstance("SHA-1")
    val result = digest.digest(toByteArray())
    val sb = StringBuilder()
    for (b in result) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}

internal fun String.escapeSingleQuotes(): String {
    val stringBuilder = StringBuilder()
    var lastSymbolWasBackSlash = false
    for (c in this) {
        if (c == SINGLE_QUOTE_CHAR && !lastSymbolWasBackSlash) {
            stringBuilder.append(BACKSLASH_CHAR)
        }

        stringBuilder.append(c)
        lastSymbolWasBackSlash = c == BACKSLASH_CHAR
    }

    return stringBuilder.toString()
}

internal val Document.resourcesNode: Node
    get() = getElementsByTagName(RESOURCES_TAG).item(0)

private const val SINGLE_QUOTE_CHAR: Char = '\''
private const val BACKSLASH_CHAR: Char = '\\'
