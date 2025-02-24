package com.avito.i18n.plugin.xml

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

internal abstract class BaseElement(
    private val node: Node
) {
    val name: String? = (node as? Element)?.getAttribute("name")

    val isTranslatable: Boolean
        get() = (node as? Element)?.getAttribute("translatable") != "false"

    fun updateDocument(document: Document) {
        document
            .documentElement
            .appendChild(document.importNode(node, true))
    }

    companion object {
        fun newInstance(element: Element): BaseElement? = when (element.tagName) {
            "string" -> StringElement(element)
            "plurals" -> PluralsElement(element)
            else -> null
        }
    }
}
