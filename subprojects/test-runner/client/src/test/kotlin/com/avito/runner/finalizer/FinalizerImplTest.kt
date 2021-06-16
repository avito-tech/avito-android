package com.avito.runner.finalizer

import com.avito.report.NoOpReportLinkGenerator
import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.runner.finalizer.action.WriteTaskVerdictAction
import com.avito.runner.scheduler.runner.model.TestSchedulerResult
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FinalizerImplTest {

    @Test
    fun `finalized - ok - single completed test`() {
        val finalizerImpl = FinalizerImpl.createStubInstance()

        val test = TestStaticDataPackage.createStubInstance()

        val testSchedulerResult = TestSchedulerResult(
            testsToRun = listOf(test),
            testResults = listOf(AndroidTest.Completed.createStubInstance(testStaticData = test))
        )

        val result = finalizerImpl.finalize(testSchedulerResult)

        assertThat(result).isInstanceOf<Finalizer.Result.Ok>()
    }

    @Test
    fun `finalized - lost - single test to run, no results`(@TempDir output: File) {
        val verdictFile = File(output, "verdict")

        val finalizerImpl = createFinalizer(verdictFile)

        val test = TestStaticDataPackage.createStubInstance()

        val testSchedulerResult = TestSchedulerResult(
            testsToRun = listOf(test),
            testResults = emptyList()
        )

        val result = finalizerImpl.finalize(testSchedulerResult)

        assertThat(result).isInstanceOf<Finalizer.Result.Failure>()
        assertThat(verdictFile.readText()).contains("${test.name} ${test.device} NOT REPORTED")
    }

    @Test
    fun `finalized - failure - single test with incident`(@TempDir output: File) {
        val verdictFile = File(output, "verdict")

        val finalizerImpl = createFinalizer(verdictFile)

        val test = TestStaticDataPackage.createStubInstance()

        val testSchedulerResult = TestSchedulerResult(
            testsToRun = listOf(test),
            testResults = listOf(
                AndroidTest.Completed.createStubInstance(
                    test,
                    testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                        incident = Incident.createStubInstance()
                    )
                )
            )
        )

        val result = finalizerImpl.finalize(testSchedulerResult)

        assertThat(result).isInstanceOf<Finalizer.Result.Failure>()
        assertThat(verdictFile.readText()).contains("${test.name} ${test.device} FAILED")
    }

    private fun createFinalizer(verdictFile: File): FinalizerImpl {
        val writeTaskVerdictAction = WriteTaskVerdictAction(
            verdictDestination = verdictFile,
            reportLinkGenerator = NoOpReportLinkGenerator()
        )

        return FinalizerImpl.createStubInstance(
            actions = listOf(writeTaskVerdictAction),
            verdictFile = verdictFile
        )
    }
}
