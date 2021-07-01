package com.avito.runner.finalizer.verdict

internal object TestStatisticsCounterFactory {

    fun create(verdict: Verdict): TestStatisticsCounter = TestStatisticsCounterImpl(verdict)
}
