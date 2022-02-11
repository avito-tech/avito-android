package com.avito.robolectric.runner.description

import com.avito.test.report.listener.description.DescriptionMetaData
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.Description

internal class RobolectricDescriptionMetaDataParserTest {

    private val parser = RobolectricDescriptionMetaDataParser()
    private val description = mock<Description>()

    @Test
    fun `valid test name - parsed correctly`() {
        val fullTestName = "`test name with [spaces](and) brackets`[22][22](com.avito.test.TestClass)"

        whenever(description.displayName).thenReturn(fullTestName)
        val expectedMetadata = DescriptionMetaData(
            environment = "Robolectric-22",
            testName = "`test name with [spaces](and) brackets`[22]",
            className = "com.avito.test.TestClass"
        )

        assertThat(parser.parse(description)).isEqualTo(expectedMetadata)
    }

    @Test
    fun `invalid test name - parsing failed`() {
        val fullTestName = "`test_name(com.avito.test.TestClass)"

        whenever(description.displayName).thenReturn(fullTestName)
        assertThrows<IllegalArgumentException>("Cannot parse description: `test_name(com.avito.test.TestClass)") {
            parser.parse(description)
        }
    }
}
