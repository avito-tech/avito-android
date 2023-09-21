package com.avito.report.model

public data class TestAttempt(val testResult: AndroidTest, val executionNumber: Int) {

    public companion object {

        private const val NO_EXECUTION = -1

        public fun createWithoutExecution(testResult: AndroidTest): TestAttempt {
            return TestAttempt(testResult, NO_EXECUTION)
        }
    }
}
