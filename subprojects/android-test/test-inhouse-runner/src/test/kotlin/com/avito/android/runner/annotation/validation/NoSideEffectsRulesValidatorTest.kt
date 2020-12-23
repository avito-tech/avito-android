package com.avito.android.runner.annotation.validation

import com.avito.android.mock.MockWebServerApiRule
import com.avito.android.test.annotations.UIComponentTest
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class NoSideEffectsRulesValidatorTest {

    @Test
    fun `success - valid component test`() {
        val rules = NoSideEffectsRulesValidator.validate(ValidTest::class.java)

        assertThat(rules).isEmpty()
    }

    @Test
    fun `finds rule - test with non-hermetic rule`() {
        val rules = NoSideEffectsRulesValidator.validate(TestWithNonHermeticRule::class.java)

        assertThat(rules).hasSize(1)
        assertThat(rules.first()).isEqualTo(NonHermeticRule::class.java)
    }

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
