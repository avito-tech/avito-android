package com.avito.runner.scheduler.suite.filter

import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.scheduler.runner.model.TestWithTarget
import com.avito.runner.scheduler.suite.filter.TestsFilter.Result.Excluded
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

internal interface FilterInfoWriter {
    fun writeFilterConfig(config: InstrumentationFilterData)
    fun writeAppliedFilter(filter: TestsFilter)
    fun writeFilterExcludes(excludes: List<Pair<TestWithTarget, Excluded>>)

    class Impl(
        outputDir: File,
    ) : FilterInfoWriter {

        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        private val filterDir: File = File(outputDir, "filter").apply { mkdir() }
        private val filterConfig: File = File(filterDir, "filter-config.json")
        private val filterApplied = File(filterDir, "filters-applied.json")
        private val filterExcludesFile = File(filterDir, "filters-excludes.json")

        override fun writeFilterConfig(config: InstrumentationFilterData) {
            filterConfig.writeText(gson.toJson(config))
        }

        override fun writeAppliedFilter(filter: TestsFilter) {
            filterApplied.writeText(gson.toJson(filter))
        }

        override fun writeFilterExcludes(excludes: List<Pair<TestWithTarget, Excluded>>) {
            filterExcludesFile.writeText(
                gson.toJson(
                    excludes.groupBy(
                        keySelector = { (_, excludeReason) ->
                            excludeReason.byFilter
                        },
                        valueTransform = { (test, _) ->
                            mapOf(
                                "testName" to test.test.name.name,
                                "device" to test.test.device.name
                            )
                        }
                    )
                )
            )
        }
    }
}
