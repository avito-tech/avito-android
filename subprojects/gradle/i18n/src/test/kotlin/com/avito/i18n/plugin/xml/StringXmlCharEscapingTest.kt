package com.avito.i18n.plugin.xml

import com.avito.i18n.plugin.xml.TestUtils.createXml
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class StringXmlCharEscapingTest {

    @Test
    fun `appending strings with single quotes - single quotes escaped`() {
        val xml = TestUtils.createXmlString {
            appendString("test1", "Test S'tring")
            appendString("test2", "'Test 2 String'")
            appendString("test3", "Test 3'' String")
        }.createXml()

        val stringElements = xml.elements.filterIsInstance<StringElement>()
        assertThat(stringElements.find { it.name == "test1" }?.value).isEqualTo("Test S\\'tring")
        assertThat(stringElements.find { it.name == "test2" }?.value).isEqualTo("\\'Test 2 String\\'")
        assertThat(stringElements.find { it.name == "test3" }?.value).isEqualTo("Test 3\\'\\' String")
    }

    @Test
    fun `appending already escaped strings - strings unchanged`() {
        val xml = TestUtils.createXmlString {
            appendString("test", "\\'Test S\\'tring")
            appendPlurals(
                "test_plurals",
                mapOf(
                    "one" to "Test \\'plural",
                    "other" to "Other plural"
                )
            )
        }.createXml()

        val stringElements = xml.elements.filterIsInstance<StringElement>()
        assertThat(stringElements.find { it.name == "test" }?.value).isEqualTo("\\'Test S\\'tring")

        val pluralElement = xml.elements.filterIsInstance<PluralsElement>().find { it.name == "test_plurals" }!!
        assertThat(pluralElement.items["one"]).isEqualTo("Test \\'plural")
        assertThat(pluralElement.items["other"]).isEqualTo("Other plural")
    }

    @Test
    fun `appending plurals with single quotes - single quotes escaped`() {
        val xml = TestUtils.createXmlString {
            appendPlurals(
                "test_plurals",
                mapOf(
                    "one" to "'Test plural'",
                    "other" to "Test p'lural"
                )
            )
        }.createXml()

        val plural = xml.elements.filterIsInstance<PluralsElement>().find { it.name == "test_plurals" }!!
        assertThat(plural.items["one"]).isEqualTo("\\'Test plural\\'")
        assertThat(plural.items["other"]).isEqualTo("Test p\\'lural")
    }
}
