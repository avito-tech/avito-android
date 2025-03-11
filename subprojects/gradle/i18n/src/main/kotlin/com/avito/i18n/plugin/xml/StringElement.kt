package com.avito.i18n.plugin.xml

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

internal class StringElement : BaseElement {

    private val _node: Element

    override val node: Node
        get() = _node

    private val _hash by lazy { value.hashSha1() }

    override val hash: String
        get() = _node.getAttribute("hash").ifEmpty { _hash }

    val value: String
        get() = _node.childNodes.takeIf { it.length > 0 }?.item(0)?.nodeValue ?: ""

    constructor(document: Document, name: String, value: String, hash: String) : super() {
        _node = document.createElement("string").apply {
            setAttribute("name", name)
            setAttribute("hash", hash.ifEmpty { value.hashSha1() })
            appendChild(document.createTextNode(value))
        }
        document.resourcesNode.appendChild(_node)
    }

    constructor(element: Element) : super() {
        _node = element
    }
}
