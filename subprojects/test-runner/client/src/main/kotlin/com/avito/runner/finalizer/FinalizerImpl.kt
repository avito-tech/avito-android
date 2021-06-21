package com.avito.runner.finalizer

import com.avito.report.model.TestStaticData
import com.avito.runner.finalizer.action.FinalizeAction
import com.avito.runner.finalizer.verdict.Verdict
import com.avito.runner.finalizer.verdict.VerdictDeterminer
import com.avito.runner.scheduler.runner.model.TestRunnerResults
import com.avito.runner.scheduler.runner.scheduler.TestSchedulerResult
import java.io.File

internal class FinalizerImpl(
    private val actions: List<FinalizeAction>,
    private val verdictFile: File,
    private val verdictDeterminer: VerdictDeterminer,
    private val finalizerFileDumper: FinalizerFileDumper,
) : Finalizer {

    override fun finalize(testSchedulerResults: TestRunnerResults): TestSchedulerResult {

        val initialTestSuite: Set<TestStaticData> = testSchedulerResults.testsToRun.toSet()

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
                TestSchedulerResult.Ok

            is Verdict.Failure ->
                TestSchedulerResult.Failure("Instrumentation task failed. Look at verdict in the file: $verdictFile")
        }
    }

    companion object
}
