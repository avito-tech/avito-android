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

    private val _inlineMarkupQuantities = mutableSetOf<String>()

    val inlineMarkupQuantities: Set<String>
        get() = _inlineMarkupQuantities

    constructor(
        document: Document,
        name: String,
        values: Map<String, String>,
        hash: String,
        markupQuantities: Set<String>,
    ) : super() {
        _node = document.createElement("plurals").apply {
            setAttribute("name", name)
            createItems(document, values, markupQuantities)
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
                val quantity = item.getAttribute("quantity")
                val itemValue = if (item.hasElementChildren()) {
                    _inlineMarkupQuantities += quantity
                    item.innerXml()
                } else {
                    item.childNodes.item(0)?.nodeValue.orEmpty()
                }
                _items += quantity to itemValue
            }
        }
    }

    private fun Map<String, String>.generateHash(): String {
        return map { it.value }.joinToString("").hashSha1()
    }

    private fun Node.createItems(document: Document, values: Map<String, String>, markupQuantities: Set<String>) {
        for ((key, value) in values) {
            val element = document.createElement("item")
            element.setAttribute("quantity", key)
            when (val content = value.toStringContent(asMarkup = key in markupQuantities)) {
                is StringContent.Markup -> {
                    element.setAttribute(MARKUP_ATTRIBUTE, "true")
                    element.appendChild(document.createTextNode(content.xml))
                    _inlineMarkupQuantities += key
                    _items += key to content.xml
                }
                is StringContent.Literal -> {
                    element.appendChild(document.createTextNode(content.text))
                    _items += key to content.text
                }
            }
            appendChild(element)
        }
    }
}
