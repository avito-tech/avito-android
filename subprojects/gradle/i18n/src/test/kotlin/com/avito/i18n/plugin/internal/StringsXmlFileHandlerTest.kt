package com.avito.i18n.plugin.internal

import com.avito.i18n.plugin.xml.PluralsElement
import com.avito.i18n.plugin.xml.StringElement
import com.avito.i18n.plugin.xml.StringsXmlFile
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.stream.StreamResult

class StringsXmlFileHandlerTest {

    @Test
    fun `updateTargetWithTranslations - untranslated markup string - no double escaping`() {
        val sourceXml = """
            <resources><string name="x"><u>Подробнее</u></string></resources>
        """.trimIndent().toStringsXml()
        val targetXml = """
            <resources><string name="x"><u>don\'t</u></string></resources>
        """.trimIndent().toStringsXml()

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = emptyList(),
            languageTag = "en"
        )

        val stringElement = result.elements.single() as StringElement
        assertThat(stringElement.value).isEqualTo("""<u>don\'t</u>""")
        assertThat(result.serialize()).contains("""<u>don\'t</u>""")
        assertThat(result.serialize()).doesNotContain("&lt;u&gt;")
    }

    @Test
    fun `updateTargetWithTranslations - untranslated plurals - keeps per-quantity form`() {
        val sourceXml = """
            <resources>
                <plurals name="p">
                    <item quantity="one"><b>%d</b> file</item>
                    <item quantity="other"><![CDATA[<b>%d</b> files]]></item>
                </plurals>
            </resources>
        """.trimIndent().toStringsXml()
        val targetXml = """
            <resources>
                <plurals name="p">
                    <item quantity="one"><b>%d</b> fayl</item>
                    <item quantity="other">&lt;b&gt;%d&lt;/b&gt; fayllar</item>
                </plurals>
            </resources>
        """.trimIndent().toStringsXml()

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = emptyList(),
            languageTag = "en"
        )

        val plurals = result.elements.single() as PluralsElement
        assertThat(plurals.inlineMarkupQuantities).containsExactly("one")

        val xml = result.serialize()
        assertThat(xml).contains("<b>%d</b> fayl</item>")
        assertThat(xml).contains("&lt;b&gt;%d&lt;/b&gt; fayllar")
    }

    @Test
    fun `updateTargetWithTranslations - when translation exists for string element - should use translated text`() {
        val sourceXml = StringsXmlFileHandlerTestData.SOURCE_XML_STRING.toStringsXml()
        val targetXml = StringsXmlFileHandlerTestData.TARGET_XML_STRING.toStringsXml()
        val translatedTextUnits = StringsXmlFileHandlerTestData.TRANSLATED_TEXT_UNITS_STRING

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = translatedTextUnits,
            languageTag = StringsXmlFileHandlerTestData.LANGUAGE_TAG
        )

        assertThat(result.elements).hasSize(1)
        assertThat(result.elements[0]).isInstanceOf(StringElement::class.java)
        val stringElement = result.elements[0] as StringElement
        assertThat(stringElement.name).isEqualTo("test_key")
        assertThat(stringElement.value).isEqualTo("translated value")
    }

    @Test
    fun `updateTargetWithTranslations - when translation exists for plurals element - should use translated text`() {
        val sourceXml = StringsXmlFileHandlerTestData.SOURCE_XML_PLURALS.toStringsXml()
        val targetXml = StringsXmlFileHandlerTestData.TARGET_XML_PLURALS.toStringsXml()
        val translatedTextUnits = StringsXmlFileHandlerTestData.TRANSLATED_TEXT_UNITS_PLURALS

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = translatedTextUnits,
            languageTag = StringsXmlFileHandlerTestData.LANGUAGE_TAG
        )

        assertThat(result.elements).hasSize(1)
        assertThat(result.elements[0]).isInstanceOf(PluralsElement::class.java)
        val pluralsElement = result.elements[0] as PluralsElement
        assertThat(pluralsElement.name).isEqualTo("test_plurals")
        assertThat(pluralsElement.items["one"]).isEqualTo("1 translated item")
        assertThat(pluralsElement.items["other"]).isEqualTo("%d translated items")
    }

    @Test
    fun `updateTargetWithTranslations - when no translation exists for element - should use target element`() {
        val sourceXml = StringsXmlFileHandlerTestData.SOURCE_XML_NO_TRANSLATION.toStringsXml()
        val targetXml = StringsXmlFileHandlerTestData.TARGET_XML_NO_TRANSLATION.toStringsXml()
        val translatedTextUnits = StringsXmlFileHandlerTestData.EMPTY_TRANSLATED_TEXT_UNITS

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = translatedTextUnits,
            languageTag = StringsXmlFileHandlerTestData.LANGUAGE_TAG
        )

        assertThat(result.elements).hasSize(1)
        assertThat(result.elements[0]).isInstanceOf(StringElement::class.java)
        val stringElement = result.elements[0] as StringElement
        assertThat(stringElement.name).isEqualTo("test_key")
        assertThat(stringElement.value).isEqualTo("target value")
    }

    @Test
    fun `updateTargetWithTranslations - when translation not found for language - should use target element`() {
        val sourceXml = StringsXmlFileHandlerTestData.SOURCE_XML_WRONG_LANGUAGE.toStringsXml()
        val targetXml = StringsXmlFileHandlerTestData.TARGET_XML_WRONG_LANGUAGE.toStringsXml()
        val translatedTextUnits = StringsXmlFileHandlerTestData.TRANSLATED_TEXT_UNITS_WRONG_LANGUAGE

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = translatedTextUnits,
            languageTag = StringsXmlFileHandlerTestData.LANGUAGE_TAG
        )

        assertThat(result.elements).hasSize(1)
        assertThat(result.elements[0]).isInstanceOf(StringElement::class.java)
        val stringElement = result.elements[0] as StringElement
        assertThat(stringElement.name).isEqualTo("test_key")
        assertThat(stringElement.value).isEqualTo("target value")
    }

    @Test
    fun `updateTargetWithTranslations - when element has no name - should be skipped`() {
        val sourceXml = StringsXmlFileHandlerTestData.SOURCE_XML_NO_NAME.toStringsXml()
        val targetXml = StringsXmlFileHandlerTestData.TARGET_XML_NO_NAME.toStringsXml()
        val translatedTextUnits = StringsXmlFileHandlerTestData.EMPTY_TRANSLATED_TEXT_UNITS

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = translatedTextUnits,
            languageTag = StringsXmlFileHandlerTestData.LANGUAGE_TAG
        )

        assertThat(result.elements).hasSize(0)
    }

    @Test
    fun `updateTargetWithTranslations - when translation not found for key - should use target element`() {
        val sourceXml = StringsXmlFileHandlerTestData.SOURCE_XML_WRONG_KEY.toStringsXml()
        val targetXml = StringsXmlFileHandlerTestData.TARGET_XML_WRONG_KEY.toStringsXml()
        val translatedTextUnits = StringsXmlFileHandlerTestData.TRANSLATED_TEXT_UNITS_WRONG_KEY

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = translatedTextUnits,
            languageTag = StringsXmlFileHandlerTestData.LANGUAGE_TAG
        )

        assertThat(result.elements).hasSize(1)
        assertThat(result.elements[0]).isInstanceOf(StringElement::class.java)
        val stringElement = result.elements[0] as StringElement
        assertThat(stringElement.name).isEqualTo("test_key")
        assertThat(stringElement.value).isEqualTo("target value")
    }

    @Test
    fun `updateTargetWithTranslations - mixed elements with and without translations - should handle correctly`() {
        val sourceXml = StringsXmlFileHandlerTestData.SOURCE_XML_MIXED.toStringsXml()
        val targetXml = StringsXmlFileHandlerTestData.TARGET_XML_MIXED.toStringsXml()
        val translatedTextUnits = StringsXmlFileHandlerTestData.TRANSLATED_TEXT_UNITS_MIXED

        val result = StringsXmlFileHelper.updateTargetWithTranslations(
            sourceStringsXmlFile = sourceXml,
            targetStringsXmlFile = targetXml,
            translatedTextUnits = translatedTextUnits,
            languageTag = StringsXmlFileHandlerTestData.LANGUAGE_TAG
        )

        assertThat(result.elements).hasSize(3)

        // Check translated string
        val translatedString = result.elements.find { it.name == "translated_key" }
        assertThat(translatedString).isInstanceOf(StringElement::class.java)
        assertThat((translatedString as StringElement).value).isEqualTo("translated string")

        // Check untranslated string (should use target value)
        val untranslatedString = result.elements.find { it.name == "untranslated_key" }
        assertThat(untranslatedString).isInstanceOf(StringElement::class.java)
        assertThat((untranslatedString as StringElement).value).isEqualTo("target value 2")

        // Check translated plural
        val translatedPlural = result.elements.find { it.name == "plural_key" }
        assertThat(translatedPlural).isInstanceOf(PluralsElement::class.java)
        val pluralElement = translatedPlural as PluralsElement
        assertThat(pluralElement.items["one"]).isEqualTo("1 translated plural")
        assertThat(pluralElement.items["other"]).isEqualTo("%d translated plurals")
    }

    private fun String.toStringsXml(): StringsXmlFile {
        return StringsXmlFile(InputSource(StringReader(this)))
    }

    private fun StringsXmlFile.serialize(): String {
        val writer = StringWriter()
        write(StreamResult(writer))
        return writer.toString()
    }
}
