package com.avito.instrumentation.internal.finalizer.verdict

internal interface TestStatisticsCounter {

    fun overallDuration(): Int

    fun overallCount(): Int

    fun successCount(): Int

    fun skippedCount(): Int

    fun failureCount(): Int

    fun notReportedCount(): Int
}
