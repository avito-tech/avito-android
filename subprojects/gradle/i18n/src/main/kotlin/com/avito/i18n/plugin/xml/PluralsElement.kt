package com.avito.i18n.plugin.xml

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

internal class PluralsElement : BaseElement {

    private val _node: Element

    override val node: Node
        get() = _node

    private val _hash by lazy { items.generateHash() }

    override val hash: String
        get() = _node.getAttribute("hash").ifEmpty { _hash }

    private val _items = mutableMapOf<String, String>()

    val items: Map<String, String>
        get() = _items

    constructor(document: Document, name: String, values: Map<String, String>, hash: String) : super() {
        _node = document.createElement("plurals").apply {
            setAttribute("name", name)
            createItems(document, values)
            setAttribute("hash", hash.ifEmpty { _hash })
        }
        document.resourcesNode.appendChild(_node)
    }

    constructor(element: Element) : super() {
        _node = element
        parseItems()
    }

    private fun parseItems() {
        val nodes = _node.childNodes
        for (i in 0..<nodes.length) {
            val item = nodes.item(i)
            if (item.nodeType == Node.ELEMENT_NODE && item is Element && item.tagName == "item") {
                _items += item.getAttribute("quantity") to item.childNodes.item(0).nodeValue
            }
        }
    }

    private fun Map<String, String>.generateHash(): String {
        return map { it.value }.joinToString("").hashSha1()
    }

    private fun Node.createItems(document: Document, values: Map<String, String>) {
        for ((k, v) in values) {
            val element = document.createElement("item")
            element.setAttribute("quantity", k)
            element.appendChild(document.createTextNode(v))
            _items += k to v
            appendChild(element)
        }
    }
}
