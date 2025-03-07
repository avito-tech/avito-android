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

internal val Document.resourcesNode: Node
    get() = getElementsByTagName(RESOURCES_TAG).item(0)
