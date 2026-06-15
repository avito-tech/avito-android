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

    val isInlineMarkup: Boolean
        get() = _node.hasElementChildren()

    val value: String
        get() {
            val children = _node.childNodes
            if (children.length == 0) return ""
            return if (isInlineMarkup) {
                _node.innerXml()
            } else {
                children.item(0)?.nodeValue ?: ""
            }
        }

    constructor(document: Document, name: String, value: String, hash: String, content: StringContent) : super() {
        _node = document.createElement("string").apply {
            setAttribute("name", name)
            setAttribute("hash", hash.ifEmpty { value.hashSha1() })
            when (content) {
                is StringContent.Markup -> {
                    setAttribute(MARKUP_ATTRIBUTE, "true")
                    appendChild(document.createTextNode(content.xml))
                }
                is StringContent.Literal -> appendChild(document.createTextNode(content.text))
            }
        }
        document.resourcesNode.appendChild(_node)
    }

    constructor(element: Element) : super() {
        _node = element
    }
}
