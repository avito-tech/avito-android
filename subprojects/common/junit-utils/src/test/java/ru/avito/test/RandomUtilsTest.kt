package ru.avito.test

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import ru.avito.util.Characters
import ru.avito.util.Is
import ru.avito.util.randomInt
import ru.avito.util.randomString

@Suppress("IllegalIdentifier")
class RandomUtilsTest {

    private val cases: List<Case> = listOf(
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

        Case(allowed = Characters.Alphanumeric, forbidden = listOf(Characters.Punctuation, Characters.Special)),
        Case(allowed = Characters.Text, forbidden = listOf(Characters.Special)),
        Case(allowed = Characters.All, forbidden = emptyList())
    )

    @Test
    fun `randomString - generate string without forbidden symbols`() {
        cases.forEach { case ->
            val generated = randomString(length = 20000, symbols = case.allowed)
            val generatedChars: List<Char> = generated.toList()

            val allowedName = case.allowed.javaClass.simpleName

            val allowedChars: Array<Char> = case.allowed.chars.toTypedArray()

            assertThat("contains only $allowedName", generatedChars, hasItems(*allowedChars))

            case.forbidden.forEach {

                val forbiddenNames = case.forbidden.map { it.javaClass.simpleName }
                val forbiddenChars: Array<Char> = case.forbidden.flatMap { it.chars }.toTypedArray()

                assertThat(
                    "$allowedName doesn't contain $forbiddenNames",
                    generatedChars,
                    not(hasItems(*forbiddenChars))
                )
            }
        }
    }

    @Test
    fun `randomString - returns string with specified length`() {
        val length = randomInt(1000)

        val generated = randomString(length)

        assertThat(generated.length, Is(length))
    }

    private class Case(val allowed: Characters, val forbidden: List<Characters>)
}
