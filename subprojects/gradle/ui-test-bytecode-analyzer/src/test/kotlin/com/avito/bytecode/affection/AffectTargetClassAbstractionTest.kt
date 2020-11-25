package com.avito.bytecode.affection

import com.avito.bytecode.ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS
import com.avito.bytecode.PAGE_3
import com.avito.bytecode.PAGE_3_FIRST_IMPLEMENTATION
import com.avito.bytecode.PAGE_3_SECOND_IMPLEMENTATION
import com.avito.bytecode.PAGE_3_SUPER_SPECIAL
import com.avito.bytecode.PAGE_4
import com.avito.bytecode.PAGE_4_FIRST_IMPLEMENTATION
import com.avito.bytecode.PAGE_4_SECOND_IMPLEMENTATION
import com.avito.bytecode.SUPER_PAGE_3_FIRST_IMPLEMENTATION
import com.avito.bytecode.SUPER_PAGE_3_SECOND_IMPLEMENTATION
import com.avito.bytecode.findInvocations
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AffectTargetClassAbstractionTest {

    @Test
    fun `all target classes affected - when interface of this target class is called`() {
        val invocations = findInvocations()

        assertThat(invocations).doesNotContainKey(PAGE_3)
        assertThat(invocations).doesNotContainKey(PAGE_3_SUPER_SPECIAL)

        assertThat(invocations).containsKey(SUPER_PAGE_3_FIRST_IMPLEMENTATION)
        assertThat(invocations[SUPER_PAGE_3_FIRST_IMPLEMENTATION] as Set<String>)
            .containsAtLeast(
                "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_super_page3_implementations",
                "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_super_page3_implementations2",
                "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_super_page3_implementations3"
            )

        assertThat(invocations).containsKey(SUPER_PAGE_3_SECOND_IMPLEMENTATION)
        assertThat(invocations[SUPER_PAGE_3_SECOND_IMPLEMENTATION] as Set<String>)
            .containsAtLeast(
                "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_super_page3_implementations",
                "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_super_page3_implementations2",
                "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_super_page3_implementations3"
            )
    }

    @Test
    fun `all target classes affected - when super class of this target class is called`() {
        val invocations = findInvocations()

        assertThat(invocations).doesNotContainKey(PAGE_4)

        assertThat(invocations).containsKey(PAGE_4_FIRST_IMPLEMENTATION)
        assertThat(invocations[PAGE_4_FIRST_IMPLEMENTATION]).contains(
            "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_page4_implementations_without_abstract"
        )

        assertThat(invocations).containsKey(PAGE_4_SECOND_IMPLEMENTATION)
        assertThat(invocations[PAGE_4_SECOND_IMPLEMENTATION]).contains(
            "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_page4_implementations_without_abstract"
        )
    }

    @Test
    fun `all target classes affected - when interface (or super class) of this target class is called (nested)`() {
        val invocations = findInvocations()

        assertThat(invocations).containsKey(PAGE_3_FIRST_IMPLEMENTATION)
        assertThat(invocations[PAGE_3_FIRST_IMPLEMENTATION] as Set<String>).contains(
            "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_page3_implementations_include_special"
        )

        assertThat(invocations).containsKey(PAGE_3_SECOND_IMPLEMENTATION)
        assertThat(invocations[PAGE_3_SECOND_IMPLEMENTATION] as Set<String>).contains(
            "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_page3_implementations_include_special"
        )

        assertThat(invocations).containsKey(SUPER_PAGE_3_FIRST_IMPLEMENTATION)
        assertThat(invocations[SUPER_PAGE_3_FIRST_IMPLEMENTATION] as Set<String>).contains(
            "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_page3_implementations_include_special"
        )

        assertThat(invocations).containsKey(SUPER_PAGE_3_SECOND_IMPLEMENTATION)
        assertThat(invocations[SUPER_PAGE_3_SECOND_IMPLEMENTATION] as Set<String>).contains(
            "$ABSTRACT_TARGET_CLASS_AFFECT_TEST_CLASS.should_affect_all_page3_implementations_include_special"
        )
    }
}
