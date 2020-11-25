package com.avito.bytecode.affection

import com.avito.bytecode.ABSTRACTION_AFFECT_TEST_CLASS
import com.avito.bytecode.PAGE_1
import com.avito.bytecode.PAGE_2
import com.avito.bytecode.findInvocations
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AffectThroughAbstractionsTest {

    @Suppress("MaxLineLength")
    @Test
    fun `target class affected through abstraction - when this abstraction called in test`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_1)
        assertThat(invocations[PAGE_1] as Set<String>).contains(
            "$ABSTRACTION_AFFECT_TEST_CLASS.should_use_page1_and_page2_because_abstraction_is_used_that_has_implementations_that_affects_this_pages"
        )

        assertThat(invocations).containsKey(PAGE_2)
        assertThat(invocations[PAGE_2] as Set<String>).contains(
            "$ABSTRACTION_AFFECT_TEST_CLASS.should_use_page1_and_page2_because_abstraction_is_used_that_has_implementations_that_affects_this_pages"
        )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `target class affected through nested abstraction - when this abstraction called in test`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_1)
        assertThat(invocations[PAGE_1] as Set<String>).contains(
            "$ABSTRACTION_AFFECT_TEST_CLASS.should_use_page1_and_page2_because_nested_abstraction_is_used_that_has_implementations_that_affects_this_pages"
        )

        assertThat(invocations).containsKey(PAGE_2)
        assertThat(invocations[PAGE_2] as Set<String>).contains(
            "$ABSTRACTION_AFFECT_TEST_CLASS.should_use_page1_and_page2_because_nested_abstraction_is_used_that_has_implementations_that_affects_this_pages"
        )
    }

    @Test
    fun `implementations of base interfaces are not affected - when method called on second interface level method`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_1)
        assertThat(invocations[PAGE_1] as Set<String>).contains(
            "$ABSTRACTION_AFFECT_TEST_CLASS.should_use_page1_because_nested_implementation_has_just_page1_invocation"
        )
    }
}
