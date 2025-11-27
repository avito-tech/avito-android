package com.avito.i18n.plugin.xml

import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.stream.StreamResult

internal object TestUtils {
    fun createXmlString(block: StringsXmlFile.() -> Unit): String {
        val doc = StringsXmlFile()
            .apply(block)
        val writer = StringWriter()
        doc.write(StreamResult(writer))
        return writer.toString()
    }

    fun String.createXml(): StringsXmlFile {
        return StringsXmlFile(InputSource(StringReader(this)))
    }
}
