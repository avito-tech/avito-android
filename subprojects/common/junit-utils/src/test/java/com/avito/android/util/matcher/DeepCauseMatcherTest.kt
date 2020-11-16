package com.avito.android.util.matcher

import com.avito.android.util.matcher.DeepCauseMatcher.Companion.deepCauseMatcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DeepCauseMatcherTest {

    @Test
    fun `deepCauseMatcher - matches - first level cause`() {
        assertThat(
            Exception(Error("text")),
            deepCauseMatcher<Error>("text")
        )
    }

    @Test
    fun `deepCauseMatcher - matches - N level cause`() {
        assertThat(
            Exception(Exception(Exception(IllegalArgumentException("text")))),
            deepCauseMatcher<IllegalArgumentException>("text")
        )
    }

    @Test
    fun `deepCauseMatcher - fails - maxDepth + 1 level cause`() {
        Assertions.assertThrows(AssertionError::class.java, {
            assertThat(
                Exception(Exception(Exception(Exception(IndexOutOfBoundsException("text"))))),
                deepCauseMatcher<IllegalArgumentException>("text", maxDepth = 3)
            )
        }, "reached max level of depth")
    }

    @Test
    fun `deepCauseMatcher - fails - no class match`() {
        Assertions.assertThrows(AssertionError::class.java, {
            assertThat(
                Exception((IndexOutOfBoundsException("text"))),
                deepCauseMatcher<IllegalArgumentException>("text", maxDepth = 3)
            )
        }, "Throwable cause at level: 3 is null")
    }

    @Test
    fun `deepCauseMatcher - fails - no text match`() {
        Assertions.assertThrows(AssertionError::class.java, {
            assertThat(
                Exception((IndexOutOfBoundsException("text"))),
                deepCauseMatcher<IndexOutOfBoundsException>("wrong", maxDepth = 3)
            )
        }, "Throwable cause at level: 3 is null")
    }
}
