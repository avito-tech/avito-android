package com.avito.i18n.plugin.xml

import groovy.xml.DOMBuilder
import groovy.xml.XmlUtil
import org.gradle.api.GradleScriptException
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.OutputStreamWriter

internal class StringsFile(
    val file: File,
    elements: List<BaseElement> = emptyList()
) {
    private val _elements: MutableList<BaseElement> = elements.toMutableList()

    val elements: List<BaseElement>
        get() = _elements

    val resPath: File = file.parentFile.parentFile

    fun exists() = file.exists()

    fun parse() {
        if (!exists()) {
            return
        }

        _elements.clear()

        val parser = try {
            val reader = FileReader(file)
            DOMBuilder.parse(reader, false, true)
        } catch (e: Exception) {
            throw GradleScriptException("Error parse file: ${file.name}", e)
        }

        if (parser.documentElement.nodeName != "resources") {
            return
        }

        val nodes = parser.childNodes.item(0).childNodes
        for (i in 0..<nodes.length) {
            val item = nodes.item(i)

            val instance = when (item.nodeType) {
                Node.ELEMENT_NODE -> BaseElement.newInstance(item as Element)

                else -> null
            }
            if (instance != null) {
                _elements += instance
            }
        }
    }

    fun writeFile() {
        if (exists()) {
            return
        }

        file.parentFile.mkdirs()

        val destParser = try {
            DOMBuilder.newInstance(false, true)
                .parseText(
                    """
                        <?xml version="1.0" encoding="utf-8"?>
                        <resources></resources>
                    """.trimIndent()
                )
        } catch (e: Exception) {
            throw GradleScriptException("Error create DOMBuilder:", e)
        }

        for (e in elements) {
            e.updateDocument(destParser)
        }
        val documentElement = destParser.documentElement

        var destosw: OutputStreamWriter? = null
        try {
            val destfos = FileOutputStream(file)
            destosw = OutputStreamWriter(destfos, "UTF-8")
            XmlUtil.serialize(documentElement, destosw)
        } catch (e: Exception) {
            throw GradleScriptException("Error write file: ${file.name}", e)
        } finally {
            destosw?.close()
        }
    }

    override fun toString(): String {
        return "StringsFile(elements=$elements)"
    }
}
