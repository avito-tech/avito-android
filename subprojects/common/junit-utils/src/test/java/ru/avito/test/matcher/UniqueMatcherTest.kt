@file:Suppress("IllegalIdentifier")

package ru.avito.test.matcher

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import ru.avito.util.assertThrows
import ru.avito.util.matcher.containsOnlyUniqueElements

class UniqueMatcherTest {

    @Test
    fun `assert - is passed - if collection contains only unique entries`() {
        val list = listOf(1, 2, 3)

        assertThat(list, containsOnlyUniqueElements())
    }

    @Test
    fun `assert - is passed - if collection is empty`() {
        val list = emptyList<Any>()

        assertThat(list, containsOnlyUniqueElements())
    }

    @Test
    fun `assert - is failed - if collection contains duplicates`() {
        val list = listOf(1, 1, 2)

        @Suppress("DEPRECATION")
        val error = assertThrows(AssertionError::class.java) {
            assertThat(list, containsOnlyUniqueElements())
        }

        assertThat(error.message, containsString("contains duplicated item: <1>"))

    }
}
