package com.avito.android.test.report

import com.avito.report.model.Entry
import com.avito.time.TimeMachineProvider
import com.avito.truth.assertThat
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.concurrent.TimeUnit

class ReportSyntheticStepsTest {

    private val timeMachine = TimeMachineProvider()

    @JvmField
    @RegisterExtension
    val report = ReportTestExtension(
        timeProvider = timeMachine
    )

    private val comment = "Comment"
    private val assertionMessage = "Assertion"

    @BeforeEach
    fun before() {
        // given
        report.initTestCaseHelper()
        report.startTestCase()
    }

    @Test
    fun `when add Entries after steps than synthetic step will be created`() {
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
        timeMachine.moveForwardOn(1, TimeUnit.SECONDS) // for step ordering
        addComment(comment)
        timeMachine.moveForwardOn(1, TimeUnit.SECONDS)
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
}
