package com.avito.android.runner.report

import com.avito.report.model.AndroidTest

public data class TestAttempt(val testResult: AndroidTest, val executionNumber: Int) {

    internal companion object {

        private const val NO_EXECUTION = -1

        internal fun createWithoutExecution(testResult: AndroidTest): TestAttempt {
            return TestAttempt(testResult, NO_EXECUTION)
        }
    }
}
