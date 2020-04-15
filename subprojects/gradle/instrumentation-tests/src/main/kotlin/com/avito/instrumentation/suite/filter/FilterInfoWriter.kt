package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.suite.filter.TestsFilter.Result.Excluded
import com.avito.instrumentation.suite.model.TestWithTarget
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

interface FilterInfoWriter {
    fun writeFilterConfig(config: InstrumentationFilter.Data)
    fun writeAppliedFilter(filter: TestsFilter)
    fun writeFilterExcludes(excludes: List<Pair<TestWithTarget, Excluded>>)

    class Impl(
        outputDir: File,
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    ) : FilterInfoWriter {

        private val filterDir: File = File(outputDir, "filter").apply { mkdir() }
        private val filterConfig: File = File(filterDir, "filter-config.json")
        private val filterApplied = File(filterDir, "filters-applied.json")
        private val filterExcludesFile = File(filterDir, "filters-excludes.json")

        override fun writeFilterConfig(config: InstrumentationFilter.Data) {
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
                                "testName" to test.test.name,
                                "device" to test.test.device
                            )
                        })
                )
            )
        }
    }
}