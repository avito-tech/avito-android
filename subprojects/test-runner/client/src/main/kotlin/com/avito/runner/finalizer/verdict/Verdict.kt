package com.avito.runner.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

internal sealed class Verdict {

    abstract val testResults: Collection<AndroidTest>

    sealed class Success : Verdict() {

        data class OK(override val testResults: Collection<AndroidTest>) : Success()

        data class Suppressed(
            override val testResults: Collection<AndroidTest>,
            val notReportedTests: Collection<AndroidTest.Lost>,
            val failedTests: Collection<TestStaticData>,
        ) : Success()
    }

    data class Failure(
        override val testResults: Collection<AndroidTest>,
        val notReportedTests: Collection<AndroidTest.Lost>,
        val unsuppressedFailedTests: Collection<TestStaticData>
    ) : Verdict()
}
