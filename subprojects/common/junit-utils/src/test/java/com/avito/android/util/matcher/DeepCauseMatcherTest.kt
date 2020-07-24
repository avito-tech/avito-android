package com.avito.android.util.matcher

import com.avito.android.util.matcher.DeepCauseMatcher.Companion.deepCauseMatcher
import org.junit.Assert
import org.junit.Rule
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.rules.ExpectedException

@Disabled
class DeepCauseMatcherTest {

    // Rules don't work at JUNIT5
    @Rule @JvmField
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun `deepCauseMatcher - matches - first level cause`() {
        Assert.assertThat(
            Exception(Error("text")),
            deepCauseMatcher<Error>("text")
        )
    }

    @Test
    fun `deepCauseMatcher - matches - N level cause`() {
        Assert.assertThat(
            Exception(Exception(Exception(IllegalArgumentException("text")))),
            deepCauseMatcher<IllegalArgumentException>("text")
        )
    }

    @Test
    fun `deepCauseMatcher - fails - maxDepth + 1 level cause`() {
        exception.expect(AssertionError::class.java)
        exception.expectMessage("reached max level of depth")

        Assert.assertThat(
            Exception(Exception(Exception(Exception(IndexOutOfBoundsException("text"))))),
            deepCauseMatcher<IllegalArgumentException>("text", maxDepth = 3)
        )
    }

    @Test
    fun `deepCauseMatcher - fails - no class match`() {
        exception.expect(AssertionError::class.java)
        exception.expectMessage("Throwable cause at level: 3 is null")

        Assert.assertThat(
            Exception(IndexOutOfBoundsException("text")),
            deepCauseMatcher<IllegalArgumentException>("text", maxDepth = 3)
        )
    }

    @Test
    fun `deepCauseMatcher - fails - no text match`() {
        exception.expect(AssertionError::class.java)
        exception.expectMessage("Throwable cause at level: 3 is null")

        Assert.assertThat(
            Exception((IndexOutOfBoundsException("text"))),
            deepCauseMatcher<IndexOutOfBoundsException>("wrong", maxDepth = 3)
        )
    }
}
