package com.avito.i18n.plugin.xml

import com.avito.i18n.plugin.xml.TestUtils.createXml
import com.google.common.truth.Truth
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.Test
import org.xml.sax.InputSource
import org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath
import java.io.StringReader

internal class StringsXmlFileTest {

    @Test
    fun `create new xml document with string tag`() {
        val xml = TestUtils.createXmlString {
            appendString("test", "test value")
        }

        assertThat(xml, hasXPath("count(//resources/string)", IsEqual("1")))
        assertThat(xml, hasXPath("//resources/string", IsEqual("test value")))
        assertThat(xml, hasXPath("//resources/string/@name", IsEqual("test")))
        assertThat(xml, hasXPath("//resources/string/@hash", IsEqual("d56c753e0f8ce84ba3d3ab284628cf6594fdaa74")))
    }

    @Test
    fun `create new xml document with plurals tag`() {
        val xml = TestUtils.createXmlString {
            appendPlurals("test_plurals", mapOf("one" to "1", "other" to "other"))
        }

        assertThat(xml, hasXPath("count(//resources/plurals/item)", IsEqual("2")))
        assertThat(xml, hasXPath("//resources/plurals/@name", IsEqual("test_plurals")))
        assertThat(xml, hasXPath("//resources/plurals/@hash", IsEqual("cf3fc3b339fda3b641e01888ce040fde3cc097cb")))
        assertThat(xml, hasXPath("//resources/plurals/item[@quantity='one']", IsEqual("1")))
        assertThat(xml, hasXPath("//resources/plurals/item[@quantity='other']", IsEqual("other")))
    }

    @Test
    fun `create new xml document with plurals and string tags`() {
        val xml = TestUtils.createXmlString {
            appendString("test", "test value")
            appendPlurals("test_plurals", mapOf("one" to "1", "other" to "other"))
        }

        assertThat(xml, hasXPath("//resources/string", IsEqual("test value")))
        assertThat(xml, hasXPath("//resources/string/@name", IsEqual("test")))
        assertThat(xml, hasXPath("//resources/string/@hash", IsEqual("d56c753e0f8ce84ba3d3ab284628cf6594fdaa74")))

        assertThat(xml, hasXPath("count(//resources/plurals/item)", IsEqual("2")))
        assertThat(xml, hasXPath("//resources/plurals/@name", IsEqual("test_plurals")))
        assertThat(xml, hasXPath("//resources/plurals/@hash", IsEqual("cf3fc3b339fda3b641e01888ce040fde3cc097cb")))
        assertThat(xml, hasXPath("//resources/plurals/item[@quantity='one']", IsEqual("1")))
        assertThat(xml, hasXPath("//resources/plurals/item[@quantity='other']", IsEqual("other")))
    }

    @Test
    fun `parse xml document with plurals and string tags`() {
        val xmlStr = """
            <resources>
                <string hash="d56c753e0f8ce84ba3d3ab284628cf6594fdaa74" name="test">test value</string>
                <plurals hash="cf3fc3b339fda3b641e01888ce040fde3cc097cb" name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent()

        val doc = StringsXmlFile(InputSource(StringReader(xmlStr)))

        Truth.assertThat(doc.elements)
            .hasSize(2)
        Truth.assertThat(doc.elements[0])
            .isInstanceOf(StringElement::class.java)
        val stringElement = doc.elements[0] as StringElement
        Truth.assertThat(stringElement.value)
            .isEqualTo("test value")
        Truth.assertThat(stringElement.name)
            .isEqualTo("test")
        Truth.assertThat(stringElement.hash)
            .isEqualTo("d56c753e0f8ce84ba3d3ab284628cf6594fdaa74")
        Truth.assertThat(doc.elements[1])
            .isInstanceOf(PluralsElement::class.java)

        val pluralsElement = doc.elements[1] as PluralsElement

        Truth.assertThat(pluralsElement.name)
            .isEqualTo("test_plurals")
        Truth.assertThat(pluralsElement.hash)
            .isEqualTo("cf3fc3b339fda3b641e01888ce040fde3cc097cb")
        Truth.assertThat(pluralsElement.items)
            .hasSize(2)
        Truth.assertThat(pluralsElement.items["one"])
            .isEqualTo("1")
        Truth.assertThat(pluralsElement.items["other"])
            .isEqualTo("other")
    }

    @Test
    fun `difference between source and target - no diff`() {
        val source = """
            <resources>
                <string name="test">test value</string>
                <plurals name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val target = """
            <resources>
                <string hash="d56c753e0f8ce84ba3d3ab284628cf6594fdaa74" name="test">test value</string>
                <plurals hash="cf3fc3b339fda3b641e01888ce040fde3cc097cb" name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val diff = source.diff(target)

        Truth.assertThat(diff)
            .hasSize(0)
    }

    @Test
    fun `difference between source and target - hash diff`() {
        val source = """
            <resources>
                <string name="test">test value</string>
                <plurals name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val target = """
            <resources>
                <string hash="d56c753e0f8ce84ba" name="test">test value</string>
                <plurals hash="cf3fc3b339fda3b641" name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val diff = source.diff(target)

        Truth.assertThat(diff)
            .hasSize(2)

        Truth.assertThat(diff)
            .containsKey("test")
        Truth.assertThat(diff)
            .containsKey("test_plurals")
    }

    @Test
    fun `difference between source and target add new plurals`() {
        val source = """
            <resources>
                <string name="test">test value</string>
                <plurals name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val target = """
            <resources>
                <string hash="d56c753e0f8ce84ba3d3ab284628cf6594fdaa74" name="test">test value</string>
            </resources>
        """.trimIndent().createXml()

        val diff = source.diff(target)

        Truth.assertThat(diff)
            .hasSize(1)

        Truth.assertThat(diff)
            .containsKey("test_plurals")
    }

    @Test
    fun `difference between source and target add new string`() {
        val source = """
            <resources>
                <string name="test">test value</string>
                <plurals name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val target = """
            <resources>
                <plurals hash="cf3fc3b339fda3b641e01888ce040fde3cc097cb" name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val diff = source.diff(target)

        Truth.assertThat(diff)
            .hasSize(1)

        Truth.assertThat(diff)
            .containsKey("test")
    }

    @Test
    fun `difference between source and target add item plurals`() {
        val source = """
            <resources>
                <plurals name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="two">2</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val target = """
            <resources>
                <plurals hash="cf3fc3b339fda3b641e01888ce040fde3cc097cb" name="test_plurals">
                    <item quantity="one">1</item>
                    <item quantity="other">other</item>
                </plurals>
            </resources>
        """.trimIndent().createXml()

        val diff = source.diff(target)

        Truth.assertThat(diff)
            .hasSize(1)

        Truth.assertThat(diff)
            .containsKey("test_plurals")
    }
}
