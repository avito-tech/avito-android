package com.avito.android.plugin.build_metrics.app_build

import com.android.build.gradle.tasks.PackageApplication
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.BuildEnvironment
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.AppBuildTimeListener
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.AppBuildTimeMetadata
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.ApplicationType
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.clickstream.AppBuildTimeClickStreamEvent
import com.avito.android.plugin.build_metrics.internal.result.BuildResult
import com.avito.android.plugin.build_metrics.internal.result.BuildStatus
import com.avito.graphite.series.SeriesName
import com.google.common.truth.Truth.assertThat
import org.gradle.util.Path
import org.junit.jupiter.api.Test
import java.time.Instant

internal class AppBuildTimeListenerTest {

    private val buildOperationsResult = BuildOperationsResult(
        tasksExecutions = listOf(
            TaskExecutionResult(
                name = ":avito-app:assembleDebug",
                path = Path.path(":avito-app:assembleDebug"),
                type = PackageApplication::class.java,
                startMs = 0,
                endMs = 1800000100,
                cacheResult = TaskCacheResult.Miss(local = true, remote = true),
            ),
            TaskExecutionResult(
                name = ":cart-demo:assembleAndroidTest",
                path = Path.path(":cart-demo:assembleAndroidTest"),
                type = PackageApplication::class.java,
                startMs = 0,
                endMs = 1800000050,
                cacheResult = TaskCacheResult.Miss(local = true, remote = true),
            ),
        ),
        cacheOperations = CacheOperations(emptyList()),
        buildResult = BuildResult(
            status = BuildStatus.Success,
            startTime = Instant.ofEpochMilli(1800000000),
            configurationEndTime = Instant.now(),
            finishTime = Instant.now(),
        )
    )

    @Test
    fun `sends PackageApplicationMetric to graphite`() {
        val metrics = processGraphiteResults(buildOperationsResult)

        assertThat(metrics).containsExactly(
            GraphiteMetric(
                SeriesName
                    .create("gradle.task.type.PackageApplication", multipart = true)
                    .addTag("module_name", "avito-app")
                    .addTag("app_type", "main")
                    .addTag("build_status", "success"),
                "100"
            ),
            GraphiteMetric(
                SeriesName
                    .create("gradle.task.type.PackageApplication", multipart = true)
                    .addTag("module_name", "cart-demo")
                    .addTag("app_type", "test")
                    .addTag("build_status", "success"),
                "50"
            )
        )
    }

    @Test
    fun `sends AppBuildTimeMetric to clickstream`() {
        val metrics = processClickStreamResults(buildOperationsResult)

        assertThat(metrics).containsExactly(
            AppBuildTimeClickStreamEvent(
                duration = 100L,
                status = "success",
                appName = "avito-app",
                appType = ApplicationType.MAIN,
                devName = "ivanivanov",
                branchName = "A-1234_test",
                repoName = "test",
                environment = BuildEnvironment.LOCAL,
            ),
            AppBuildTimeClickStreamEvent(
                duration = 50L,
                status = "success",
                appName = "cart-demo",
                appType = ApplicationType.TEST,
                devName = "ivanivanov",
                branchName = "A-1234_test",
                repoName = "test",
                environment = BuildEnvironment.LOCAL,
            )
        )
    }

    private fun processGraphiteResults(result: BuildOperationsResult): List<GraphiteMetric> {
        val buildMetricSender = StubBuildMetricsSender()

        val listener = AppBuildTimeListener(
            sender = buildMetricSender,
            metadata = AppBuildTimeMetadata(
                userName = "ivanivanov",
                branchName = "A-1234_test",
                repoName = "test"
            ),
            environment = BuildEnvironment.LOCAL,
        )
        listener.onBuildFinished(result)

        return buildMetricSender.getSentGraphiteMetrics()
    }

    private fun processClickStreamResults(result: BuildOperationsResult): List<ClickStreamEvent> {
        val buildMetricSender = StubBuildMetricsSender()

        val listener = AppBuildTimeListener(
            sender = buildMetricSender,
            metadata = AppBuildTimeMetadata(
                userName = "ivanivanov",
                branchName = "A-1234_test",
                repoName = "test"
            ),
            environment = BuildEnvironment.LOCAL,
            )
        listener.onBuildFinished(result)

        return buildMetricSender.getSentClickStreamEvents()
    }
}
