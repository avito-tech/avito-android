package com.avito.deeplink_generator

import com.avito.deeplink_generator.internal.parser.DeeplinkParser
import com.avito.deeplink_generator.model.Deeplink
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeeplinkParserTest {

    @Test
    fun `parse link with scheme - default scheme empty - scheme is present in link`() {
        val link = "ru.avito://1/feed"
        val defaultScheme = ""
        val expectedLink = Deeplink("ru.avito", "1", "/feed")

        val actualLink = DeeplinkParser.parse(link, defaultScheme)

        assertThat(actualLink).isEqualTo(expectedLink)
    }

    @Test
    fun `parse link without scheme - default scheme empty - exception is thrown`() {
        val link = "1/feed"
        val defaultScheme = ""

        assertThrows<IllegalArgumentException> { DeeplinkParser.parse(link, defaultScheme) }
    }

    @Test
    fun `parse link with scheme - default scheme added - scheme is present and not overriden by default`() {
        val link = "ru.autoteka://1/feed"
        val defaultScheme = "ru.avito"
        val expectedLink = Deeplink("ru.autoteka", "1", "/feed")

        val actualLink = DeeplinkParser.parse(link, defaultScheme)

        assertThat(actualLink).isEqualTo(expectedLink)
    }

    @Test
    fun `parse link without scheme - default scheme not empty - scheme is present in link`() {
        val link = "1/feed"
        val defaultScheme = "ru.avito"
        val expectedLink = Deeplink("ru.avito", "1", "/feed")

        val actualLink = DeeplinkParser.parse(link, defaultScheme)

        assertThat(actualLink).isEqualTo(expectedLink)
    }
}
