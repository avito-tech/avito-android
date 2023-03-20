package com.avito.android.plugin.build_metrics.internal.gradle.app_build

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class PackageApplicationMetric(
    private val time: Long,
    private val status: String,
    private val module: String,
    private val appType: ApplicationType,
) : BuildMetric.Graphite() {

    enum class ApplicationType(val code: String) {
        MAIN("main"),
        TEST("test")
    }

    private val base: SeriesName = SeriesName.create(
        "gradle.task.type.PackageApplication",
        multipart = true
    )

    override fun asGraphite(): GraphiteMetric {
        val series = base
            .addTag("module_name", module)
            .addTag("app_type", appType.code)
            .addTag("build_status", status)

        return GraphiteMetric(
            path = series,
            value = time.toString()
        )
    }
}
