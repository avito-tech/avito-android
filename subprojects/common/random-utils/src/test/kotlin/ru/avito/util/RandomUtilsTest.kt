package ru.avito.util

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class RandomUtilsTest {

    @TestFactory
    fun `randomString - generate string without forbidden symbols`(): List<DynamicTest> = listOf(
        Case(
            allowed = Characters.Alphabetic,
            forbidden = listOf(Characters.Digits, Characters.Punctuation, Characters.Special)
        ),
        Case(
            allowed = Characters.Digits,
            forbidden = listOf(Characters.Alphabetic, Characters.Punctuation, Characters.Special)
        ),
        Case(
            allowed = Characters.Punctuation,
            forbidden = listOf(Characters.Alphabetic, Characters.Digits, Characters.Special)
        ),
        Case(
            allowed = Characters.Special,
            forbidden = listOf(Characters.Alphabetic, Characters.Digits, Characters.Punctuation)
        ),

        Case(
            allowed = Characters.Alphanumeric,
            forbidden = listOf(Characters.Punctuation, Characters.Special)
        ),
        Case(
            allowed = Characters.Text,
            forbidden = listOf(Characters.Special)
        ),
        Case(
            allowed = Characters.All,
            forbidden = emptyList()
        )
    ).mapIndexed { index, case ->
        DynamicTest.dynamicTest("case #$index") {
            val generated = randomString(length = 20000, symbols = case.allowed)
            val generatedChars: List<Char> = generated.toList()

            val allowedName = case.allowed.javaClass.simpleName

            val allowedChars: Array<Char> = case.allowed.chars.toTypedArray()

            assertWithMessage("contains only $allowedName")
                .that(generatedChars)
                .containsAtLeastElementsIn(allowedChars)

            case.forbidden.forEach { _ ->
                val forbiddenNames = case.forbidden.map { it.javaClass.simpleName }
                val forbiddenChars: Array<Char> = case.forbidden.flatMap { it.chars }.toTypedArray()

                assertWithMessage("$allowedName doesn't contain $forbiddenNames")
                    .that(generatedChars)
                    .containsNoneIn(forbiddenChars)
            }
        }
    }

    @Test
    fun `randomString - returns string with specified length`() {
        val length = randomInt(1000)

        val generated = randomString(length)

        assertThat(generated.length).isEqualTo(length)
    }

    private class Case(val allowed: Characters, val forbidden: List<Characters>)
}
