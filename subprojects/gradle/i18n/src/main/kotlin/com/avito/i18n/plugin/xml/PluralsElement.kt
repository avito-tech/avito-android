package com.avito.i18n.plugin.xml

import groovy.xml.DOMBuilder
import org.gradle.api.GradleScriptException
import org.w3c.dom.Element
import org.w3c.dom.Node

internal class PluralsElement(
    element: Element
) : BaseElement(element) {

    private val _values = mutableMapOf<String, String>()

    val values: Map<String, String>
        get() = _values

    init {
        val nodes = element.childNodes
        for (i in 0..<nodes.length) {
            val item = nodes.item(i)
            if (item.nodeType == Node.ELEMENT_NODE && item is Element) {
                _values += item.getAttribute("quantity") to item.childNodes.item(0).nodeValue
            }
        }
    }

    override fun toString(): String {
        return "PluralsElement(name='$name', values=$values)"
    }

    companion object {
        fun create(name: String, values: Map<String, String>): PluralsElement {
            val builder = StringBuilder("""<plurals name="$name">""")

            for ((k, v) in values) {
                builder.append("""<item quantity="$k">$v</item>""")
            }

            builder.append("</plurals>")
            return try {
                val document = DOMBuilder.newInstance(false, true)
                    .parseText(builder.toString())
                PluralsElement(document.documentElement)
            } catch (e: Exception) {
                throw GradleScriptException("Error create plurals: $name", e)
            }
        }
    }
}
