package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.rule.SimpleRule
import org.hamcrest.Matchers.isA
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleRuleTest {

    @get:Rule
    val rule = FailedRule()

    @Suppress("DEPRECATION")
    @get:Rule
    var thrown: ExpectedException = ExpectedException.none()

    @Test
    fun has_test_error__test_rule_failed_after_test_execution() {
        rule.failAfterTest = true

        thrown.expect(RuleError::class.java)
        thrown.expectCause(isA<IllegalStateException>(IllegalStateException::class.java))

        throw IllegalStateException("Error from test")
    }

    @Test
    fun has_test_error__test_failed() {
        rule.failAfterTest = false

        thrown.expect(IllegalStateException::class.java)
        thrown.expectMessage("Error from test")

        throw IllegalStateException("Error from test")
    }

    private class RuleError : java.lang.RuntimeException()

    class FailedRule : SimpleRule() {

        var failAfterTest: Boolean = false

        override fun before() {
        }

        override fun after() {
            if (failAfterTest) {
                throw RuleError()
            }
        }
    }
}
