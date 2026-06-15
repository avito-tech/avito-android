package com.avito.i18n.plugin.xml

import com.avito.i18n.plugin.xml.TestUtils.createXml
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class StringMarkupTest {

    @Test
    fun `parse string with underline tag - keeps inner markup`() {
        val xml = """
            <resources>
                <string name="underlined"><u>Подробнее</u></string>
            </resources>
        """.trimIndent().createXml()

        val element = xml.elements.filterIsInstance<StringElement>().single()
        assertThat(element.value).isEqualTo("<u>Подробнее</u>")
    }

    @Test
    fun `parse string with bold tag - keeps inner markup`() {
        val xml = """
            <resources>
                <string name="bold"><b>Важно</b></string>
            </resources>
        """.trimIndent().createXml()

        val element = xml.elements.filterIsInstance<StringElement>().single()
        assertThat(element.value).isEqualTo("<b>Важно</b>")
    }

    @Test
    fun `parse string with mixed content - keeps full inner markup`() {
        val xml = """
            <resources>
                <string name="mixed">before <b>bold</b> after</string>
            </resources>
        """.trimIndent().createXml()

        val element = xml.elements.filterIsInstance<StringElement>().single()
        assertThat(element.value).isEqualTo("before <b>bold</b> after")
    }

    @Test
    fun `string with markup has non-empty hash`() {
        val xml = """
            <resources>
                <string name="underlined"><u>Подробнее</u></string>
            </resources>
        """.trimIndent().createXml()

        val element = xml.elements.filterIsInstance<StringElement>().single()
        assertThat(element.hash).isNotEqualTo("da39a3ee5e6b4b0d3255bfef95601890afd80709")
        assertThat(element.value).isNotEmpty()
    }

    @Test
    fun `parse string with CDATA - keeps inner content`() {
        val xml = """
            <resources>
                <string name="cdata"><![CDATA[<b>bold</b> text]]></string>
            </resources>
        """.trimIndent().createXml()

        val element = xml.elements.filterIsInstance<StringElement>().single()
        assertThat(element.value).isEqualTo("<b>bold</b> text")
    }

    @Test
    fun `append string with markup - written inline and unescaped`() {
        val xmlString = TestUtils.createXmlString {
            appendString("underlined", "<u>Подробнее</u>", asMarkup = true)
        }

        assertThat(xmlString).contains("<u>Подробнее</u>")
        assertThat(xmlString).doesNotContain("&lt;u&gt;")
    }

    @Test
    fun `markup string round-trips through write and parse`() {
        val source = "before <b>bold</b> after"
        val xmlString = TestUtils.createXmlString {
            appendString("mixed", source, asMarkup = true)
        }

        val reparsed = xmlString.createXml().elements.filterIsInstance<StringElement>().single()
        assertThat(reparsed.value).isEqualTo(source)
    }

    @Test
    fun `plain string with ampersand stays escaped and hash unchanged`() {
        val xmlString = TestUtils.createXmlString {
            appendString("plain", "Tom & Jerry")
        }
        assertThat(xmlString).contains("Tom &amp; Jerry")

        val reparsed = xmlString.createXml().elements.filterIsInstance<StringElement>().single()
        assertThat(reparsed.value).isEqualTo("Tom & Jerry")
    }

    @Test
    fun `parse plurals with markup item - keeps inner markup`() {
        val xml = """
            <resources>
                <plurals name="reviews">
                    <item quantity="one"><b>%d</b> отзыв</item>
                    <item quantity="other"><b>%d</b> отзывов</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val plurals = xml.elements.filterIsInstance<PluralsElement>().single()
        assertThat(plurals.items["one"]).isEqualTo("<b>%d</b> отзыв")
        assertThat(plurals.items["other"]).isEqualTo("<b>%d</b> отзывов")
    }

    @Test
    fun `append plurals with markup - written inline and unescaped`() {
        val xmlString = TestUtils.createXmlString {
            appendPlurals(
                "reviews",
                mapOf(
                    "one" to "<b>%d</b> отзыв",
                    "other" to "<b>%d</b> отзывов",
                ),
                markupQuantities = setOf("one", "other")
            )
        }

        assertThat(xmlString).contains("<b>%d</b> отзыв")
        assertThat(xmlString).doesNotContain("&lt;b&gt;")
    }

    @Test
    fun `append markup with apostrophe in text - apostrophe escaped and stays valid xml`() {
        val xmlString = TestUtils.createXmlString {
            appendString("possessive", "<u>don't</u>", asMarkup = true)
        }

        assertThat(xmlString).contains("""<u>don\'t</u>""")
        val reparsed = xmlString.createXml().elements.filterIsInstance<StringElement>().single()
        assertThat(reparsed.value).isEqualTo("""<u>don\'t</u>""")
    }

    @Test
    fun `append markup with single-quoted attribute - not corrupted`() {
        val xmlString = TestUtils.createXmlString {
            appendString("link", "<a href='https://avito.ru'>тут</a>", asMarkup = true)
        }

        assertThat(xmlString).doesNotContain("""href=\'""")
        val reparsed = xmlString.createXml().elements.filterIsInstance<StringElement>().single()
        assertThat(reparsed.value).contains("тут")
        assertThat(reparsed.value).contains("https://avito.ru")
    }

    @Test
    fun `inline tag string is inline markup`() {
        val element = """
            <resources><string name="x"><u>Подробнее</u></string></resources>
        """.trimIndent().createXml().elements.filterIsInstance<StringElement>().single()

        assertThat(element.isInlineMarkup).isTrue()
    }

    @Test
    fun `CDATA string is not inline markup`() {
        val element = """
            <resources><string name="x"><![CDATA[<b>bold</b> text]]></string></resources>
        """.trimIndent().createXml().elements.filterIsInstance<StringElement>().single()

        assertThat(element.isInlineMarkup).isFalse()
    }

    @Test
    fun `append value with asMarkup false - tags are escaped as literal text`() {
        val xmlString = TestUtils.createXmlString {
            appendString(name = "html", value = "<b>bold</b> text", hash = "h", asMarkup = false)
        }

        assertThat(xmlString).contains("&lt;b&gt;bold&lt;/b&gt; text")
        assertThat(xmlString).doesNotContain("<b>bold</b>")
    }

    @Test
    fun `sha1 of source markup matches the value pinned in e2e test data`() {
        assertThat("<u>Подробнее</u>".hashSha1()).isEqualTo("c9951cd6766cfbca5046fe13139fa1927e926241")
    }

    @Test
    fun `plurals item with tag is inline markup but CDATA item is not`() {
        val plurals = """
            <resources>
                <plurals name="files">
                    <item quantity="one"><b>%d</b> file</item>
                    <item quantity="other"><![CDATA[<b>%d</b> files]]></item>
                </plurals>
            </resources>
        """.trimIndent().createXml().elements.filterIsInstance<PluralsElement>().single()

        assertThat(plurals.inlineMarkupQuantities).containsExactly("one")
        assertThat(plurals.items["one"]).isEqualTo("<b>%d</b> file")
        assertThat(plurals.items["other"]).isEqualTo("<b>%d</b> files")
    }

    @Test
    fun `append plurals - quantity outside markupQuantities written as escaped literal`() {
        val xmlString = TestUtils.createXmlString {
            appendPlurals(
                name = "files",
                values = mapOf("other" to "<b>%d</b> files"),
                markupQuantities = emptySet()
            )
        }

        assertThat(xmlString).contains("&lt;b&gt;%d&lt;/b&gt; files")
        assertThat(xmlString).doesNotContain("<b>%d</b> files")
    }
}
