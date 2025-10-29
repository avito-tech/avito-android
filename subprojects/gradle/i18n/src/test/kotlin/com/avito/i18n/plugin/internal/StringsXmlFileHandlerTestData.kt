package com.avito.i18n.plugin.internal

import com.avito.i18n.plugin.dto.TranslatedTextUnit
import com.avito.i18n.plugin.dto.TranslatedTextWithLang
import com.avito.i18n.plugin.dto.TranslationPluralText
import com.avito.i18n.plugin.dto.TranslationText
import org.intellij.lang.annotations.Language

internal object StringsXmlFileHandlerTestData {

    const val LANGUAGE_TAG = "en"

    @Language("XML")
    val SOURCE_XML_STRING = """
        <resources>
            <string name="test_key">test value</string>
        </resources>
    """.trimIndent()

    @Language("XML")
    val TARGET_XML_STRING = """
        <resources>
            <string name="test_key" hash="old_hash">old value</string>
        </resources>
    """.trimIndent()

    val TRANSLATED_TEXT_UNITS_STRING = listOf(
        TranslatedTextUnit(
            key = "test_key",
            component = "test",
            namespace = "test",
            status = 1,
            translatedTexts = listOf(
                TranslatedTextWithLang(
                    lang = LANGUAGE_TAG,
                    translationText = TranslationText.Text("translated value")
                )
            ),
            error = null
        )
    )

    @Language("XML")
    val SOURCE_XML_PLURALS = """
        <resources>
            <plurals name="test_plurals">
                <item quantity="one">1 item</item>
                <item quantity="other">%d items</item>
            </plurals>
        </resources>
    """.trimIndent()

    @Language("XML")
    val TARGET_XML_PLURALS = """
        <resources>
            <plurals name="test_plurals" hash="old_hash">
                <item quantity="one">1 old item</item>
                <item quantity="other">%d old items</item>
            </plurals>
        </resources>
    """.trimIndent()

    val TRANSLATED_TEXT_UNITS_PLURALS = listOf(
        TranslatedTextUnit(
            key = "test_plurals",
            component = "test",
            namespace = "test",
            status = 1,
            translatedTexts = listOf(
                TranslatedTextWithLang(
                    lang = LANGUAGE_TAG,
                    translationText = TranslationText.Plural(
                        TranslationPluralText(
                            zero = null,
                            one = "1 translated item",
                            two = null,
                            few = null,
                            many = null,
                            other = "%d translated items"
                        )
                    )
                )
            ),
            error = null
        )
    )

    @Language("XML")
    val SOURCE_XML_NO_TRANSLATION = """
        <resources>
            <string name="test_key">test value</string>
        </resources>
    """.trimIndent()

    @Language("XML")
    val TARGET_XML_NO_TRANSLATION = """
        <resources>
            <string name="test_key" hash="target_hash">target value</string>
        </resources>
    """.trimIndent()

    val EMPTY_TRANSLATED_TEXT_UNITS = emptyList<TranslatedTextUnit>()

    @Language("XML")
    val SOURCE_XML_WRONG_LANGUAGE = """
        <resources>
            <string name="test_key">test value</string>
        </resources>
    """.trimIndent()

    @Language("XML")
    val TARGET_XML_WRONG_LANGUAGE = """
        <resources>
            <string name="test_key" hash="target_hash">target value</string>
        </resources>
    """.trimIndent()

    val TRANSLATED_TEXT_UNITS_WRONG_LANGUAGE = listOf(
        TranslatedTextUnit(
            key = "test_key",
            component = "test",
            namespace = "test",
            status = 1,
            translatedTexts = listOf(
                TranslatedTextWithLang(
                    lang = "es", // Different language
                    translationText = TranslationText.Text("translated value")
                )
            ),
            error = null
        )
    )

    @Language("XML")
    val SOURCE_XML_NO_NAME = """
        <resources>
            <string>value without name</string>
        </resources>
    """.trimIndent()

    @Language("XML")
    val TARGET_XML_NO_NAME = """
        <resources>
        </resources>
    """.trimIndent()

    @Language("XML")
    val SOURCE_XML_WRONG_KEY = """
        <resources>
            <string name="test_key">test value</string>
        </resources>
    """.trimIndent()

    @Language("XML")
    val TARGET_XML_WRONG_KEY = """
        <resources>
            <string name="test_key" hash="target_hash">target value</string>
        </resources>
    """.trimIndent()

    val TRANSLATED_TEXT_UNITS_WRONG_KEY = listOf(
        TranslatedTextUnit(
            key = "different_key", // Different key
            component = "test",
            namespace = "test",
            status = 1,
            translatedTexts = listOf(
                TranslatedTextWithLang(
                    lang = LANGUAGE_TAG,
                    translationText = TranslationText.Text("translated value")
                )
            ),
            error = null
        )
    )

    @Language("XML")
    val SOURCE_XML_MIXED = """
        <resources>
            <string name="translated_key">original value</string>
            <string name="untranslated_key">original value</string>
            <plurals name="plural_key">
                <item quantity="one">1 item</item>
                <item quantity="other">%d items</item>
            </plurals>
        </resources>
    """.trimIndent()

    @Language("XML")
    val TARGET_XML_MIXED = """
        <resources>
            <string name="translated_key" hash="old_hash">target value 1</string>
            <string name="untranslated_key" hash="old_hash">target value 2</string>
            <plurals name="plural_key" hash="old_hash">
                <item quantity="one">1 target item</item>
                <item quantity="other">%d target items</item>
            </plurals>
        </resources>
    """.trimIndent()

    val TRANSLATED_TEXT_UNITS_MIXED = listOf(
        TranslatedTextUnit(
            key = "translated_key",
            component = "test",
            namespace = "test",
            status = 1,
            translatedTexts = listOf(
                TranslatedTextWithLang(
                    lang = LANGUAGE_TAG,
                    translationText = TranslationText.Text("translated string")
                )
            ),
            error = null
        ),
        TranslatedTextUnit(
            key = "plural_key",
            component = "test",
            namespace = "test",
            status = 1,
            translatedTexts = listOf(
                TranslatedTextWithLang(
                    lang = LANGUAGE_TAG,
                    translationText = TranslationText.Plural(
                        TranslationPluralText(
                            zero = null,
                            one = "1 translated plural",
                            two = null,
                            few = null,
                            many = null,
                            other = "%d translated plurals"
                        )
                    )
                )
            ),
            error = null
        )
    )
}
