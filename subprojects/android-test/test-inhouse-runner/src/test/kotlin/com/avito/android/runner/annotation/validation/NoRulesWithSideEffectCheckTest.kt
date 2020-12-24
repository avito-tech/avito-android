package com.avito.android.runner.annotation.validation

import com.avito.android.mock.MockWebServerApiRule
import com.avito.android.runner.annotation.resolver.TestMethodOrClass
import com.avito.android.test.annotations.UIComponentTest
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class NoRulesWithSideEffectCheckTest {

    @Test
    fun `success - valid component test`() {
        validate(ValidTest::class.java)
    }

    @Test
    fun `fail - test with non-hermetic rule`() {
        val error = assertThrows<IllegalStateException> {
            validate(TestWithNonHermeticRule::class.java)
        }

        assertThat(error).hasMessageThat().contains(
            "Test com.avito.android.runner.annotation.validation.NoRulesWithSideEffectCheckTest.TestWithNonHermeticRule"
                + "uses rules with side effects: NonHermeticRule. "
                + "It makes test unstable. Replace these rules by hermetic equivalents or change type of test."
        )
    }

    private fun validate(testClass: Class<*>) =
        NoRulesWithSideEffectCheck().validate(
            TestMethodOrClass(testClass)
        )

    @UIComponentTest
    class ValidTest {

        @get:Rule
        val mockApi = MockWebServerApiRule()

        @org.junit.Test
        fun test() {
        }
    }

    @UIComponentTest
    class TestWithNonHermeticRule {

        @get:Rule
        val unstableRule = NonHermeticRule()

        @org.junit.Test
        fun test() {
        }
    }

    class NonHermeticRule : TestRule, HasSideEffects {

        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    base.evaluate()
                }
            }
        }
    }
}
