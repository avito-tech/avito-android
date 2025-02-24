package com.avito.i18n.plugin.xml

import groovy.xml.DOMBuilder
import org.gradle.api.GradleScriptException
import org.w3c.dom.Element

internal class StringElement(
    private val element: Element
) : BaseElement(element) {

    val text: String
        get() = element.childNodes.item(0).nodeValue

    override fun toString(): String {
        return "StringElement(name '$name', text='$text')"
    }

    companion object {
        fun create(name: String, value: String): StringElement {
            return try {
                val document = DOMBuilder.newInstance(false, true)
                    .parseText(
                        """<string name="$name">$value</string>"""
                    )
                StringElement(document.documentElement)
            } catch (e: Exception) {
                throw GradleScriptException("Error create string: $name", e)
            }
        }
    }
}
