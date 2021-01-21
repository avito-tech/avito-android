package com.avito.android.elastic

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ElasticDateFormatCheckerTest {

    @Test
    fun `correct formatting`() {
        val checker = ElasticDateFormatChecker()

        assertDoesNotThrow {
            checker.check("2020-01-20")
        }
    }

    @TestFactory
    fun `incorrect formatting`(): List<DynamicTest> {
        val checker = ElasticDateFormatChecker()

        return listOf(
            "2020-01-0020",
            "2020-1-20",
            "2020-0001-0020"
        ).map {
            dynamicTest(it) {
                assertThrows<IllegalArgumentException> {
                    checker.check(it)
                }
            }
        }
    }
}
