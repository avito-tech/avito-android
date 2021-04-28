package com.avito.android.test.report

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.StepResult
import com.avito.report.model.Entry
import com.avito.time.TimeMachineProvider
import com.avito.truth.assertThat
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.concurrent.TimeUnit

@ExtendWith(StepDslExtension::class)
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
        step("Real step")

        report.addEntriesOutOfStep()

        val state = report.currentState as Started
        report.finishTestCase()

        // then
        state.assertStep(
            stepsCount = 2,
            preconditionsCount = 0,
            stepIndex = 1,
            stepTitle = "Out of step",
            stepEntriesCount = 3
        )
    }

    @Test
    fun `when add htmlEntry before steps - synthetic step will be added to preconditions`() {
        // when
        report.addEntriesOutOfStep()

        step("Real step")
        val state = report.currentState as Started
        report.finishTestCase()

        // then
        state.assertPrecondition(
            stepsCount = 1,
            preconditionsCount = 1,
            preconditionIndex = 0,
            preconditionTitle = "Out of step",
            preconditionEntriesCount = 3
        )
    }

    @Test
    fun `when add htmlEntry between steps than synthetic step will be created`() {
        // when
        step("Real step")

        report.addEntriesOutOfStep()

        step("Real step")
        val state = report.currentState as Started
        report.finishTestCase()

        // then
        state.assertStep(
            stepsCount = 3,
            preconditionsCount = 0,
            stepIndex = 1,
            stepTitle = "Out of step",
            stepEntriesCount = 3
        )
    }

    private fun InternalReport.addEntriesOutOfStep() {
        addHtml("label", "content")
        timeMachine.moveForwardOn(1, TimeUnit.SECONDS) // for step ordering
        addComment(comment)
        timeMachine.moveForwardOn(1, TimeUnit.SECONDS)
        addAssertion(assertionMessage)
    }

    private fun Started.assertStep(
        stepsCount: Int,
        preconditionsCount: Int,
        stepIndex: Int,
        stepTitle: String,
        stepEntriesCount: Int
    ) {
        assertThat(testCaseStepList).hasSize(stepsCount)
        assertThat(preconditionStepList).hasSize(preconditionsCount)
        val step = testCaseStepList[stepIndex]

        step.assertStep(
            title = stepTitle,
            entriesCount = stepEntriesCount
        )
    }

    private fun Started.assertPrecondition(
        stepsCount: Int,
        preconditionsCount: Int,
        preconditionIndex: Int,
        preconditionTitle: String,
        preconditionEntriesCount: Int
    ) {
        assertThat(testCaseStepList).hasSize(stepsCount)
        assertThat(preconditionStepList).hasSize(preconditionsCount)
        val precondition = preconditionStepList[preconditionIndex]

        precondition.assertStep(
            title = preconditionTitle,
            entriesCount = preconditionEntriesCount
        )
    }

    private fun StepResult.assertStep(
        title: String,
        entriesCount: Int
    ) {
        assertThat(this.title)
            .isEqualTo(title)

        assertThat(attachments.entries)
            .hasSize(entriesCount)

        assertThat<Entry.File>(attachments.entries[0]) {
            assertThat(fileType).isEqualTo(Entry.File.Type.html)
        }

        assertThat<Entry.Comment>(attachments.entries[1]) {
            assertThat(this.title).isEqualTo(comment)
        }

        assertThat<Entry.Check>(attachments.entries[2]) {
            assertThat(this.title).isEqualTo(assertionMessage)
        }
    }
}
