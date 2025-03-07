package com.avito.i18n.plugin.xml

import org.w3c.dom.Element
import org.w3c.dom.Node

internal abstract class BaseElement {
    protected abstract val node: Node

    val name: String?
        get() = (node as? Element)?.getAttribute("name")

    val isTranslatable: Boolean
        get() = (node as? Element)?.getAttribute("translatable") != "false"

    abstract val hash: String

    override fun toString(): String {
        return "Element(name=$name, isTranslatable=$isTranslatable, hash='$hash')"
    }
}
