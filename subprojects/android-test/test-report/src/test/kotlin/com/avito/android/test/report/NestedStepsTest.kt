package com.avito.android.test.report

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class NestedStepsTest {

    @JvmField
    @RegisterExtension
    val report = ReportTestExtension()

    @BeforeEach
    fun before() {
        report.initTestCaseHelper()
        report.startTestCase()
    }

    @Test
    fun `WHEN we create a nested step THEN we should FAIL`() {
        val error = assertThrows(StepException::class.java) {
            step("Outer step", report, false) {
                step("Inner step", report, false) {

                }
            }
        }
        val cause = error.cause
        assertThat(cause).isNotNull()
        assertThat(cause).hasMessageThat()
            .containsMatch(".+Inner step.+Outer step.+Nested steps are not supported")
    }

    @Test
    fun `WHEN we create a precondition inside a step THEN we should FAIL`() {
        val error = assertThrows(StepException::class.java) {
            step("Outer step", report, false) {
                precondition("precondition", report, false) {

                }
            }
        }
        val cause = error.cause
        assertThat(cause).isNotNull()
        assertThat(cause).hasMessageThat()
            .containsMatch(".+precondition.+Outer step.+Preconditions inside steps are not supported")
    }

    @Test
    fun `WHEN we create a step and current step is Synthetic THEN we should overwrite the current step`() {
        // WHEN
        report.addComment("Comment") // that leads to the Synthetic step creation
        step("Step", report, false) {}

        // THEN
        // No errors
    }
}
