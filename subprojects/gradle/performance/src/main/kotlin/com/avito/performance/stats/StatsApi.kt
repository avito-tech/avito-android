package com.avito.performance.stats

import com.avito.performance.stats.compare.TestForComparing
import com.avito.performance.stats.comparison.ComparedTest
import com.avito.report.model.PerformanceTest
import com.avito.utils.logging.CILogger

internal interface StatsApi {

    fun mde(perfTests: List<PerformanceTest>)

    fun compare(toCompare: List<TestForComparing>): List<ComparedTest.Result>

    class Impl(
        private val url: String,
        private val verbose: Boolean,
        private val logger: CILogger,
        private val requestProvider: RequestProvider = RequestProvider(
            url = url,
            httpClient = HttpClientProvider(logger).getHttpClient(verbose),
            gson = GsonProvider().getGson()
        )
    ) : StatsApi {

        override fun mde(perfTests: List<PerformanceTest>) {
            requestProvider.request<Any>("/1/mde", perfTests)
        }

        override fun compare(toCompare: List<TestForComparing>): List<ComparedTest.Result> {
            return requestProvider.request("/2/validate?stat=mw", toCompare)
        }
    }
}
