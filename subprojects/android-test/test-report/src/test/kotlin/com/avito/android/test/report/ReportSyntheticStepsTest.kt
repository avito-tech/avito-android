package com.avito.android.test.report

import com.avito.report.model.Entry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class ReportSyntheticStepsTest {

    @get:Rule
    val report = ReportRule()

    private val comment = "Comment"
    private val assertionMessage = "Assertion"

    @Test
    fun `when add Entries after steps than synthetic step will be created`() {
        // given
        report.initTestCaseHelper()
        report.startTestCase()

        // when
        step("Real step", report, false) {}

        report.addEntriesOutOfStep()

        val state = report.reportTestCase()

        // then
        state.assertThat(
            stepsCount = 2,
            syntheticStepIndex = 1,
            syntheticStepTitle = "Synthetic step",
            syntheticStepEntriesCount = 3
        )
    }

    @Test
    fun `when add htmlEntry before steps than synthetic step will be created`() {
        // given
        report.initTestCaseHelper()
        report.startTestCase()

        // when
        report.addEntriesOutOfStep()

        step("Real step", report, false) {}
        val state = report.reportTestCase()

        // then
        state.assertThat(
            stepsCount = 2,
            syntheticStepIndex = 0,
            syntheticStepTitle = "Synthetic step",
            syntheticStepEntriesCount = 3
        )
    }

    @Test
    fun `when add htmlEntry between steps than synthetic step will be created`() {
        // given
        report.initTestCaseHelper()
        report.startTestCase()

        // when
        step("Real step", report, false) {}

        report.addEntriesOutOfStep()

        step("Real step", report, false) {}
        val state = report.reportTestCase()

        // then
        state.assertThat(
            stepsCount = 3,
            syntheticStepIndex = 1,
            syntheticStepTitle = "Synthetic step",
            syntheticStepEntriesCount = 3
        )
    }

    private fun Report.addEntriesOutOfStep() {
        addHtml("label", "content")
        Thread.sleep(10) // for step ordering
        addComment(comment)
        Thread.sleep(10) // for step ordering
        addAssertion(assertionMessage)
    }


    private fun ReportState.Initialized.Started.assertThat(
        stepsCount: Int,
        syntheticStepIndex: Int,
        syntheticStepTitle: String,
        syntheticStepEntriesCount: Int
    ) {
        assertThat(testCaseStepList).hasSize(stepsCount)
        val syntheticStep = testCaseStepList[syntheticStepIndex]

        assertThat(syntheticStep.title)
            .isEqualTo(syntheticStepTitle)

        assertThat(syntheticStep.entryList)
            .hasSize(syntheticStepEntriesCount)

        assertThat<Entry.File>(syntheticStep.entryList[0]) {
            assertThat(fileType).isEqualTo(Entry.File.Type.html)
        }

        assertThat<Entry.Comment>(syntheticStep.entryList[1]) {
            assertThat(title).isEqualTo(comment)
        }

        assertThat<Entry.Check>(syntheticStep.entryList[2]) {
            assertThat(title).isEqualTo(assertionMessage)
        }
    }

    private inline fun <reified T> assertThat(any: Any, assert: T.() -> Unit) {
        assertThat(any).isInstanceOf(T::class.java)
        assert(any as T)
    }
}
