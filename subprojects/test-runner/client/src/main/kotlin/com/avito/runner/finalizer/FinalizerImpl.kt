package com.avito.runner.finalizer

import com.avito.report.model.TestStaticData
import com.avito.runner.finalizer.action.FinalizeAction
import com.avito.runner.finalizer.verdict.Verdict
import com.avito.runner.finalizer.verdict.VerdictDeterminer
import com.avito.runner.scheduler.runner.scheduler.TestsScheduler
import java.io.File

internal class FinalizerImpl(
    private val actions: List<FinalizeAction>,
    private val verdictFile: File,
    private val verdictDeterminer: VerdictDeterminer,
    private val finalizerFileDumper: FinalizerFileDumper,
) : Finalizer {

    override fun finalize(testSchedulerResults: TestsScheduler.Result): Finalizer.Result {

        val initialTestSuite: Set<TestStaticData> = testSchedulerResults.testSuite.testsToRun.map { it.test }.toSet()

        finalizerFileDumper.dump(
            initialTestSuite = initialTestSuite,
            testResults = testSchedulerResults.testResults
        )

        val verdict: Verdict = verdictDeterminer.determine(
            initialTestSuite = initialTestSuite,
            testResults = testSchedulerResults.testResults
        )

        actions.forEach { action -> action.action(verdict = verdict) }

        return when (verdict) {
            is Verdict.Success ->
                Finalizer.Result.Ok

            is Verdict.Failure ->
                Finalizer.Result.Failure("Instrumentation task failed. Look at verdict in the file: $verdictFile")
        }
    }
}
