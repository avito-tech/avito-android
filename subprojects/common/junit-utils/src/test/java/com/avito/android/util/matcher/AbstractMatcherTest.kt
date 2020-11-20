package com.avito.android.util.matcher

import org.hamcrest.Matcher
import org.junit.jupiter.api.Test

abstract class AbstractMatcherTest {

    protected abstract fun createMatcher(): Matcher<*>

    @Test
    fun `test is null safe`() {
        assertNullSafe(createMatcher())
    }

    @Test
    fun `test copes with unknown types`() {
        createMatcher().matches(UnknownType())
    }

}
