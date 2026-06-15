package com.avito.i18n.plugin.xml

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

internal const val RESOURCES_TAG = "resources"

internal const val MARKUP_ATTRIBUTE = "__markup"

private const val FRAGMENT_WRAPPER_TAG = "fragment"
private const val DISALLOW_DOCTYPE_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl"

private val documentBuilderFactory = ThreadLocal.withInitial {
    DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        isValidating = false
        setFeature(DISALLOW_DOCTYPE_FEATURE, true)
    }
}

internal val transformerFactory = ThreadLocal.withInitial { TransformerFactory.newInstance() }

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

internal fun newDocumentBuilder(): DocumentBuilder = documentBuilderFactory.get().newDocumentBuilder()

internal fun Node.hasElementChildren(): Boolean {
    val children = childNodes
    for (i in 0 until children.length) {
        if (children.item(i).nodeType == Node.ELEMENT_NODE) {
            return true
        }
    }
    return false
}

internal fun Node.innerXml(): String {
    val children = childNodes
    if (children.length == 0) return ""
    val transformer = transformerFactory.get().newTransformer().apply {
        setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        setOutputProperty(OutputKeys.METHOD, "xml")
        setOutputProperty(OutputKeys.ENCODING, "UTF-8")
    }
    val builder = StringBuilder()
    for (i in 0 until children.length) {
        val writer = StringWriter()
        transformer.transform(DOMSource(children.item(i)), StreamResult(writer))
        builder.append(writer.toString())
    }
    return builder.toString()
}

internal fun String.toStringContent(asMarkup: Boolean): StringContent {
    val literal = StringContent.Literal(escapeSingleQuotes())
    if (!asMarkup) return literal
    val wrapped = "<$FRAGMENT_WRAPPER_TAG>$this</$FRAGMENT_WRAPPER_TAG>"
    val fragment = runCatching {
        newDocumentBuilder().parse(InputSource(StringReader(wrapped))).documentElement
    }.getOrElse { return literal }
    if (!fragment.hasElementChildren()) return literal
    fragment.escapeSingleQuotesInTextNodes()
    return StringContent.Markup(fragment.innerXml())
}

private fun Node.escapeSingleQuotesInTextNodes() {
    val children = childNodes
    for (i in 0 until children.length) {
        val child = children.item(i)
        when (child.nodeType) {
            Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> child.nodeValue = child.nodeValue?.escapeSingleQuotes()
            Node.ELEMENT_NODE -> child.escapeSingleQuotesInTextNodes()
            else -> Unit
        }
    }
}

private const val SINGLE_QUOTE_CHAR: Char = '\''
private const val BACKSLASH_CHAR: Char = '\\'
