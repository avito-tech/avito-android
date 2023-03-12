package com.avito.android.plugin.build_metrics

import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import java.io.File

internal class BuildMetricsRunner(
    private val projectDir: File
) {

    fun build(args: List<String>): TestResult {
        return gradlew(
            projectDir,
            *args.toTypedArray(),
            "-Pavito.build.metrics.enabled=true",
            "-Pavito.stats.enabled=false",
            "-Pstatsd.test",
            "-Pavito.graphite.enabled=true",
            "-Pavito.graphite.namespace=build.metrics.test",
            "-Pavito.graphite.host",
            "-Pavito.graphite.port=80",
            "-Pbuild.metrics.test"
        )
    }
}
