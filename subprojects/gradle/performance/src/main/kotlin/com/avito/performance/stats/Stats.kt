package com.avito.performance.stats

import com.avito.performance.stats.compare.TestForComparing
import com.avito.performance.stats.comparison.ComparedTest
import com.avito.report.model.PerformanceTest
import org.funktionale.tries.Try

internal interface Stats {

    fun compare(toCompare: List<TestForComparing>): Try<List<ComparedTest.Result>>

    fun mde(perfTests: List<PerformanceTest>)

    class Impl(private val api: StatsApi) : Stats {

        override fun mde(perfTests: List<PerformanceTest>) {
            api.mde(perfTests)
        }

        override fun compare(toCompare: List<TestForComparing>): Try<List<ComparedTest.Result>> {
            return Try { api.compare(toCompare) }
        }
    }
}
