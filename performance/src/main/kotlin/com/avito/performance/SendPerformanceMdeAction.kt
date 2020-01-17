package com.avito.performance

import com.avito.performance.stats.Stats
import com.avito.performance.stats.StatsApi
import com.avito.report.model.PerformanceTest
import com.avito.utils.logging.CILogger
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import java.io.File
import java.io.Serializable
import javax.inject.Inject

open class SendPerformanceMdeAction(
    private val params: Params,
    logger: CILogger
) : Runnable {

    private val stats: Stats = Stats.Impl(
        api = StatsApi.Impl(
            url = params.url,
            logger = logger,
            verbose = false
        )
    )

    data class Params(
        val url: String,
        val logger: CILogger,
        val currentTests: File
    ) : Serializable {
        companion object
    }

    @Inject
    constructor(params: Params) : this(params, params.logger)

    override fun run() {
        with(Gson().fromJson<List<PerformanceTest>>(params.currentTests.readText())) {
            stats.mde(this)
        }
    }
}
