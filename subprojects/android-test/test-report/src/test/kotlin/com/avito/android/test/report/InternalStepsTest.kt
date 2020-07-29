package com.avito.android.test.report

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class InternalStepsTest {
    @JvmField
    @RegisterExtension
    val report = ReportTestExtension()

    @BeforeEach
    fun before() {
        report.initTestCaseHelper()
        report.startTestCase()
    }

    @Test
    fun `WHEN we create an internal step THEN we should FAIL`() {
        Assertions.assertThrows(StepException::class.java) {
            step("Outer step", report, false) {
                step("Inner step", report, false) {

                }
            }
        }
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
