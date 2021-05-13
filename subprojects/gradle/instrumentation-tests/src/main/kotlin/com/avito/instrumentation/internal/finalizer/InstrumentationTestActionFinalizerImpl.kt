package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.finalizer.action.FinalizeAction
import com.avito.instrumentation.internal.finalizer.verdict.Verdict
import com.avito.instrumentation.internal.finalizer.verdict.VerdictDeterminer
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.utils.BuildFailer
import java.io.File

internal class InstrumentationTestActionFinalizerImpl(
    private val actions: List<FinalizeAction>,
    private val buildFailer: BuildFailer,
    private val verdictFile: File,
    private val verdictDeterminer: VerdictDeterminer,
) : InstrumentationTestActionFinalizer {

    override fun finalize(testSchedulerResults: TestsScheduler.Result) {

        val verdict: Verdict = verdictDeterminer.determine(
            initialTestSuite = testSchedulerResults.initialTestSuite,
            testResults = testSchedulerResults.testResults
        )

        actions.forEach { action -> action.action(verdict = verdict) }

        when (verdict) {
            is Verdict.Failure -> buildFailer.failBuild(
                "Instrumentation task failed. Look at verdict in the file: $verdictFile"
            )
            is Verdict.Success -> {
                // do nothing
            }
        }
    }
}
