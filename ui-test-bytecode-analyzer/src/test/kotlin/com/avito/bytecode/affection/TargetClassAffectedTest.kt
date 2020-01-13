package com.avito.bytecode.affection

import com.avito.bytecode.COMMON_TEST_CLASS
import com.avito.bytecode.IGNORED_TEST_CLASS
import com.avito.bytecode.PAGE_1
import com.avito.bytecode.PAGE_2
import com.avito.bytecode.TESTS_WITHOUT_IMPACT_TO_TARGET_CLASS_KEY
import com.avito.bytecode.WITHOUT_IMPACT_TEST_CLASS
import com.avito.bytecode.findInvocations
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TargetClassAffectedTest {

    @Test
    fun `target class usage detected - when it called on test method`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_2)

        assertThat(invocations[PAGE_2] as Set<String>)
            .contains("$COMMON_TEST_CLASS.should_use_page1_by_before_and_page2_by_invoke")
    }

    @Test
    fun `all tests should be affected by screens - when this screen affected by method marked as before`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_1)

        assertThat(invocations[PAGE_1] as Set<String>)
            .containsAtLeast(
                "$COMMON_TEST_CLASS.should_use_page1_by_before_and_page2_by_invoke",
                "$COMMON_TEST_CLASS.should_use_just_page1_by_before"
            )
    }

    @Test
    fun `target class usage detected - when it called inside ignored test method`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_2)
        assertThat(invocations[PAGE_2] as Set<String>)
            .contains("$IGNORED_TEST_CLASS.should_use_page2_by_invoke_even_with_ignore_annotation")
    }

    @Test
    fun `test saved on wildcard category - when it doesn't used`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(TESTS_WITHOUT_IMPACT_TO_TARGET_CLASS_KEY)
        assertThat(invocations[TESTS_WITHOUT_IMPACT_TO_TARGET_CLASS_KEY] as Set<String>)
            .contains("$WITHOUT_IMPACT_TEST_CLASS.using_nothing_test_method")
    }
}
