package com.avito.bytecode.affection

import com.avito.bytecode.IMPLEMENTED_TEST_CLASS
import com.avito.bytecode.PAGE_1
import com.avito.bytecode.PAGE_3_FIRST_IMPLEMENTATION
import com.avito.bytecode.PAGE_3_SECOND_IMPLEMENTATION
import com.avito.bytecode.SUPER_PAGE_3_FIRST_IMPLEMENTATION
import com.avito.bytecode.SUPER_PAGE_3_SECOND_IMPLEMENTATION
import com.avito.bytecode.findInvocations
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ImplementedTestClassesTest {

    @Test
    fun `target class usage detected - when it called test method from abstraction class`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_1)
        assertThat(invocations[PAGE_1] as Set<String>)
            .contains("$IMPLEMENTED_TEST_CLASS.should_use_page1_on_implementation_test_class")
    }

    @Test
    fun `target class usage detected - when it called test method from abstraction class through abstraction target class`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_3_FIRST_IMPLEMENTATION)
        assertThat(invocations[PAGE_3_FIRST_IMPLEMENTATION] as Set<String>)
            .containsAtLeast(
                "$IMPLEMENTED_TEST_CLASS.should_use_all_implementations_of_page3_on_implementation_test_class",
                "$IMPLEMENTED_TEST_CLASS.should_use_all_implementations_of_page3_on_implementation_test_class_again"
            )

        assertThat(invocations).containsKey(PAGE_3_SECOND_IMPLEMENTATION)
        assertThat(invocations[PAGE_3_SECOND_IMPLEMENTATION] as Set<String>)
            .containsAtLeast(
                "$IMPLEMENTED_TEST_CLASS.should_use_all_implementations_of_page3_on_implementation_test_class",
                "$IMPLEMENTED_TEST_CLASS.should_use_all_implementations_of_page3_on_implementation_test_class_again"
            )

        assertThat(invocations).containsKey(SUPER_PAGE_3_FIRST_IMPLEMENTATION)
        assertThat(invocations[SUPER_PAGE_3_FIRST_IMPLEMENTATION] as Set<String>)
            .containsAtLeast(
                "$IMPLEMENTED_TEST_CLASS.should_use_all_implementations_of_page3_on_implementation_test_class",
                "$IMPLEMENTED_TEST_CLASS.should_use_all_implementations_of_page3_on_implementation_test_class_again"
            )

        assertThat(invocations).containsKey(SUPER_PAGE_3_SECOND_IMPLEMENTATION)
        assertThat(invocations[SUPER_PAGE_3_SECOND_IMPLEMENTATION] as Set<String>)
            .containsAtLeast(
                "$IMPLEMENTED_TEST_CLASS.should_use_all_implementations_of_page3_on_implementation_test_class",
                "$IMPLEMENTED_TEST_CLASS.should_use_all_implementations_of_page3_on_implementation_test_class_again"
            )
    }
}
