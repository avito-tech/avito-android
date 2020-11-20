package com.avito.android.plugin.build_metrics

import com.avito.android.gradle.metric.AbstractMetricsConsumer
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.Operation
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.TimeMetric
import com.avito.android.stats.graphiteSeriesElement
import com.avito.kotlin.dsl.isRoot
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.internal.tasks.TaskExecutionOutcome.EXECUTED
import org.gradle.api.internal.tasks.TaskExecutionOutcome.FROM_CACHE
import org.gradle.api.internal.tasks.TaskStateInternal
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.measureTimeMillis

internal class AggregatedMetricsConsumer(
    project: Project,
    private val stats: BuildMetricTracker
) : AbstractMetricsConsumer() {

    private val log = project.logger
    private val env = project.buildEnvironment
    private val androidApps: MutableList<String> = CopyOnWriteArrayList()

    private val BuildResult.isBuildAction
        get() = action == "Build"

    private val TaskExecution.internalState: TaskStateInternal
        get() = try {
            state as TaskStateInternal
        } catch (e: Exception) {
            error(
                "Task $path has unsupported class $javaClass. " +
                    "You can disable build-metrics plugin by project property 'avito.build.metrics.enabled=false'"
            )
        }

    init {
        check(project.isRoot()) { "Project ${project.path} must be root" }

        project.subprojects.forEach { module ->
            module.pluginManager.withPlugin("com.android.application") {
                androidApps.add(module.path)
            }
        }
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        if (!buildResult.isBuildAction) return

        buildResult.gradle?.startParameter?.let { startParameter ->
            if (startParameter.isDryRun) {
                log.debug("Build metrics are disabled due to dry run")
                return
            }
            if (isBuildNothing(startParameter.taskNames)) {
                log.debug("Build metrics are disabled due to useless tasks: ${startParameter.taskRequests}")
                return
            }
        }
        val totalMs = measureTimeMillis {
            trackConfigurationTime(buildResult, profile)
            trackBuildTime(buildResult, profile)
            trackBuildTimeByRequestedTasks(buildResult, profile)
            trackTasksMetrics(buildResult, profile)
        }
        log.debug("send metrics took $totalMs ms")
    }

    private fun isBuildNothing(tasks: List<String>): Boolean {
        if (tasks.isEmpty()) return true

        val taskNames = tasks.map { it.substringAfterLast(':') }
        val uselessTasks = listOf(
            "help",
            "tasks",
            "clean",
            "cleanBuildCache",
            "dependencies"
        )
        return taskNames.all { name ->
            uselessTasks.contains(name)
        }
    }

    private fun trackConfigurationTime(buildResult: BuildResult, profile: BuildProfile) {
        stats.track(
            buildResult,
            TimeMetric("init_configuration.total", profile.initWithConfigurationTimeMs)
        )
    }

    private fun trackBuildTime(buildResult: BuildResult, profile: BuildProfile) {
        stats.track(
            buildResult,
            TimeMetric("build-time.total", profile.elapsedTotal)
        )
    }

    private fun trackBuildTimeByRequestedTasks(buildResult: BuildResult, profile: BuildProfile) {
        val tasks: List<String> = buildResult.gradle?.startParameter?.taskNames.orEmpty()
            .sorted()
            .filterNot { defaultBuildTasks.contains(it) }
            .map { it.removePrefix(":") }

        // todo тут правильно смотреть макс длину события, вместе с тегами сейчас 1000
        val canBeTooLongForGraphite = tasks.size > 2
        val tasksShorthand = if (canBeTooLongForGraphite) {
            "_"
        } else {
            graphiteSeriesElement(tasks.joinToString(separator = "_"))
        }
        stats.track(
            buildResult,
            TimeMetric("build-tasks.$tasksShorthand", profile.elapsedTotal)
        )
    }

    private fun trackTasksMetrics(buildResult: BuildResult, profile: BuildProfile) {
        if (env is BuildEnvironment.Mirkale) return // contains only upload and download tasks

        val tasks = profile.getProjects()
            .flatMap { it.getTasks() }
            .filterNot { it.path.startsWith(":buildSrc") }
            .filter { it.internalState.isActionable }

        // TODO: filter only cacheable tasks after https://github.com/gradle/gradle/issues/9333
        val executed = tasks.count {
            it.state!!.didWork
                && it.internalState.outcome == EXECUTED
        }
        val hits = tasks.count {
            it.internalState.outcome == FROM_CACHE
        }
        val missedPercentages = ((executed.toFloat() / (executed + hits)) * 100).toLong()
        stats.track(buildResult, GaugeMetric("tasks.from_cache.miss", missedPercentages))

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
                    && it.internalState.outcome == EXECUTED
                    && it.elapsedTime > considerableTimeMs
            }
            .sortedWith(Operation.slowestFirst())
            .take(256)
            .forEach {
                trackExecutedTask(buildResult, it)
            }
    }

    private fun name(path: String) = path.substringAfterLast(':')

    private fun trackExecutedTask(buildResult: BuildResult, task: TaskExecution) {
        val name = name(task.path)
        val module = graphiteSeriesElement(task.path.substringBeforeLast(':'))
            .removePrefix("_")
            .let {
                if (it.isEmpty()) "_" else it
            }
        stats.track(buildResult, TimeMetric("tasks.executed.$module.$name.total", task.elapsedTime))
    }

    @Suppress("unused")
    private fun dump(task: TaskExecution) {
        val state = task.internalState
        log.debug(
            "TaskExecution: ${task.path}, " +
                "didWork:${state.didWork}, " +
                "actionable:${state.isActionable}, " +
                "outcome:${state.outcome}"
        )
    }
}

private const val considerableTimeMs = 100

/**
 * Добавляются неявно к каждой сборке, зашумляют метрики.
 */
private val defaultBuildTasks = listOf("checkBuildEnvironment")
