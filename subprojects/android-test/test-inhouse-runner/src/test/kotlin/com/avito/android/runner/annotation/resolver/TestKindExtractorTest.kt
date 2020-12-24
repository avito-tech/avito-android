package com.avito.android.runner.annotation.resolver

import com.avito.android.runner.annotation.resolver.MethodStringRepresentation.Resolution
import com.avito.android.test.annotations.E2ETest
import com.avito.android.test.annotations.UIComponentTest
import com.avito.report.model.Kind
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestKindExtractorTest {

    @Test
    fun `parse valid test`() {
        val kind = extract(ComponentTest::class.java)

        assertThat(kind).isEqualTo(Kind.UI_COMPONENT)
    }

    @Test
    fun `fail - test with multiple types`() {

        val error = assertThrows<IllegalArgumentException> {
            extract(MultipleTypesTest::class.java)
        }

        assertThat(error).hasMessageThat().contains("has multiple types")
        assertThat(error).hasMessageThat().contains("UIComponentTest")
        assertThat(error).hasMessageThat().contains("E2ETest")
    }

    private fun extract(test: Class<*>): Kind =
        TestKindExtractor.extract(Resolution.ClassOnly(test))

    @UIComponentTest
    class ComponentTest {

        @org.junit.Test
        fun test() {
        }
    }

    @UIComponentTest
    @E2ETest
    class MultipleTypesTest {

        @org.junit.Test
        fun test() {
        }
    }
}
