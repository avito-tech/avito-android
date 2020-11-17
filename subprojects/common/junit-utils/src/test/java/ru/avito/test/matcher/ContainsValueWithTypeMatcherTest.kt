@file:Suppress("IllegalIdentifier")

package ru.avito.test.matcher

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import ru.avito.util.assertThrows
import ru.avito.util.matcher.containsValueWithType

class ContainsValueWithTypeMatcherTest {

    @Test
    fun `assert - is passed - if collection contains element with desired type`() {
        val list = listOf("1", 2)

        assertThat(list, containsValueWithType<String>())
    }

    @Test
    fun `assert - is failed - if collection doesn't contains element with desired type`() {
        val list = listOf(1, 2)

        val error = assertThrows<AssertionError>() {
            assertThat(list, containsValueWithType<String>())
        }

        assertThat(error.message, containsString("contains item with type <class kotlin.String>"))
    }

    @Test
    fun `assert - is failed - if collection is empty`() {
        val list = emptyList<Any>()

        val error = assertThrows<AssertionError>() {
            assertThat(list, containsValueWithType<String>())
        }

        assertThat(error.message, containsString("contains item with type <class kotlin.String>"))
    }
}
