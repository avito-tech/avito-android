package com.avito.i18n.plugin.xml

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

internal class StringsXmlFile {

    private val document: Document
    private val _elements = mutableListOf<BaseElement>()
    private val rootNode: Node

    val elements: List<BaseElement>
        get() = _elements

    constructor() {
        val builder = newDocumentBuilder()
        document = builder.newDocument()
        rootNode = document.createElement(RESOURCES_TAG)
        document.appendChild(rootNode)
    }

    constructor(inputSource: InputSource) {
        val builder = newDocumentBuilder()
        document = builder.parse(inputSource)
        rootNode = document.getElementsByTagName(RESOURCES_TAG).item(0)
        parseDocument()
    }

    private fun parseDocument() {
        if (document.documentElement.nodeName != RESOURCES_TAG) {
            return
        }
        val nodes = document.childNodes.item(0).childNodes
        for (i in 0..<nodes.length) {
            val item = nodes.item(i)
            val instance = when (item.nodeType) {
                Node.ELEMENT_NODE -> createElement(item as Element)
                else -> null
            }
            if (instance != null) {
                _elements += instance
            }
        }
    }

    private fun createElement(element: Element): BaseElement? = when (element.tagName) {
        "string" -> StringElement(element)
        "plurals" -> PluralsElement(element)
        else -> null
    }

    fun appendString(name: String, value: String, hash: String = "", asMarkup: Boolean = false): StringsXmlFile {
        _elements += StringElement(document, name, value, hash, value.toStringContent(asMarkup))
        return this
    }

    fun appendPlurals(
        name: String,
        values: Map<String, String>,
        hash: String = "",
        markupQuantities: Set<String> = emptySet(),
    ): StringsXmlFile {
        _elements += PluralsElement(document, name, values, hash, markupQuantities)
        return this
    }

    fun diff(target: StringsXmlFile): Map<String, BaseElement> {
        val l = elements.associateBy { it.name!! }
        val r = target.elements.associateBy { it.name!! }

        val result = mutableMapOf<String, BaseElement>()
        for (e in l) {
            if (!e.value.isTranslatable || r[e.key]?.hash == e.value.hash) {
                continue
            }
            result[e.key] = e.value
        }
        return result
    }

    fun write(result: StreamResult) {
        document.xmlStandalone = true
        val transformer = transformerFactory.get()
            .newTransformer(StreamSource(StringReader(XSL_STYLE)))
        transformer.transform(DOMSource(document), result)
    }

    companion object {
        private val XSL_STYLE = """
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                <xsl:strip-space elements="*"/>
                <xsl:output method="xml" indent="yes" standalone="yes" encoding="UTF-8"/>

                <xsl:template match="@*|node()">
                    <xsl:copy>
                        <xsl:apply-templates select="@*|node()"/>
                    </xsl:copy>
                </xsl:template>

                <xsl:template match="string[@$MARKUP_ATTRIBUTE='true']">
                    <string>
                        <xsl:apply-templates select="@*[name()!='$MARKUP_ATTRIBUTE']"/>
                        <xsl:value-of select="." disable-output-escaping="yes"/>
                    </string>
                </xsl:template>

                <xsl:template match="item[@$MARKUP_ATTRIBUTE='true']">
                    <item>
                        <xsl:apply-templates select="@*[name()!='$MARKUP_ATTRIBUTE']"/>
                        <xsl:value-of select="." disable-output-escaping="yes"/>
                    </item>
                </xsl:template>

            </xsl:stylesheet>
        """.trimIndent()
    }
}
