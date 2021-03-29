package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.Operation
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.plugin.build_metrics.BuildMetricTracker
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.TimeMetric
import com.avito.math.percentOf
import org.gradle.api.internal.tasks.TaskExecutionOutcome

internal class SlowTasksListener(
    private val metricTracker: BuildMetricTracker
) : BuildResultListener {

    override fun onBuildFinished(status: BuildMetricTracker.BuildStatus, profile: BuildProfile) {
        val tasks = profile.getProjects()
            .flatMap { it.getTasks() }
            .filter { it.internalState.isActionable }

        // TODO: filter only cacheable tasks after https://github.com/gradle/gradle/issues/9333 (MBS-7244)
        val executed = tasks.count {
            it.state!!.didWork
                && it.internalState.outcome == TaskExecutionOutcome.EXECUTED
        }
        val hits = tasks.count {
            it.internalState.outcome == TaskExecutionOutcome.FROM_CACHE
        }
        val missedPercentages = executed.percentOf(executed + hits).toLong()
        val metric = GaugeLongMetric(SeriesName.create("tasks", "from_cache", "miss"), missedPercentages)

        metricTracker.track(status, metric)

        // Other states are not needed yet
        // If you want to find same results as in build scan report use these filters:
        // | Outcome    | Filter
        // | FAILED     | state.failure != null
        // | SUCCESS    | state.failure == null && state.didWork && internalState.outcome == EXECUTED
        // | NO-SOURCE, SKIPPED, UP-TO-DATE | state != FROM_CACHE && state.isSkipped

        tasks.asSequence()
            .filter {
                // We ignore state and cacheability due to focus on execution time
                it.state!!.didWork
                    && it.internalState.outcome == TaskExecutionOutcome.EXECUTED
                    && it.elapsedTime > considerableTimeMs
            }
            .sortedWith(Operation.slowestFirst())
            .take(TRACKING_LIMIT)
            .forEach { task ->
                metricTracker.track(status, createTaskExecutionEvent(task))
            }
    }

    private fun createTaskExecutionEvent(task: TaskExecution) = TimeMetric(
        name = SeriesName.create("tasks", "executed")
            .append(task.module.toSeriesName())
            .append(task.name)
            .append("total"),
        timeInMs = task.elapsedTime
    )
}

/**
 * There are a lot of tasks, and we need only this amount of slowest tasks to track
 */
private const val TRACKING_LIMIT = 256

private const val considerableTimeMs = 100
