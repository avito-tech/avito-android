package com.avito.android.build_checks.internal.params

import com.avito.android.build_checks.internal.BuildEnvironmentInfo
import com.avito.android.build_checks.pluginId
import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.statsd
import org.gradle.api.Project

// TODO: Will be removed in MBS-12691
internal class GradlePropertiesChecker(
    private val project: Project,
    private val envInfo: BuildEnvironmentInfo
) {

    fun check() {
        project.afterEvaluate {
            val tracker = buildTracker(project)
            val propertiesChecks = listOf(
                GradlePropertiesCheck(project, envInfo) // TODO: extract to a task
            )
            propertiesChecks.forEach { checker ->
                checker.getMismatches()
                    .onSuccess {
                        it.forEach { mismatch ->
                            project.logger.warn(
                                "${mismatch.name} differs from recommended value! " +
                                    "Recommended: ${mismatch.expected} " +
                                    "Actual: ${mismatch.actual}"
                            )
                            val safeParamName = mismatch.name.replace(".", "-")
                            tracker.track(
                                CountMetric(SeriesName.create("configuration", "mismatch", safeParamName))
                            )
                        }
                    }
                    .onFailure {
                        project.logger.error("[$pluginId] can't check project", it)
                        val checkerName = checker.javaClass.simpleName
                        tracker.track(
                            CountMetric(SeriesName.create("configuration", "mismatch", "failed", checkerName))
                        )
                    }
            }
        }
    }

    private fun buildTracker(project: Project): BuildMetricTracker {
        return BuildMetricTracker(project.environmentInfo().get(), project.statsd.get())
    }
}
