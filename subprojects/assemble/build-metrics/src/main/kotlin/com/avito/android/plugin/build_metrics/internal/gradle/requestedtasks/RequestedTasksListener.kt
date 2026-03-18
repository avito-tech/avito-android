package com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender

internal class RequestedTasksListener(
    private val sender: BuildMetricSender,
    private val requestedTasks: List<String>,
    private val bootstrapRequestedTaskNames: Set<String>,
    private val metadata: RequestedTasksMetadata,
    private val executionHistory: BuildExecutionHistory,
    private val hardwareInfoProvider: HardwareInfoProvider,
) : BuildOperationsResultListener {

    override val name: String = "RequestedTasks"

    override fun onBuildFinished(result: BuildOperationsResult) {
        val userTag = metadata.userName
        val hardwareInfo = hardwareInfoProvider.hardwareInfo
        val durationMs = result.buildResult.buildDuration.toMillis()

        val previousState = executionHistory.readPreviousState()
        val isCommitChanged = previousState?.let { it.commit != metadata.commitHash } ?: false

        val requestedTasksWithoutBootstrap = requestedTasks.filterNot { isBootstrapTask(it) }

        // assembleDebug/assembleRelease have higher priority: when present, report only those task(s).
        // Gradle sync is detected by an empty requested tasks list; we send "gradleSync" in this case.
        val tasksToReport = when {
            requestedTasksWithoutBootstrap.isEmpty() ->
                listOf("gradleSync")
            requestedTasksWithoutBootstrap.any { it.endsWith("assembleDebug") || it.endsWith("assembleRelease") } ->
                requestedTasksWithoutBootstrap
                    .filter { it.endsWith("assembleDebug") || it.endsWith("assembleRelease") }
                    .map { sanitizeTaskName(it) }
            else ->
                requestedTasksWithoutBootstrap.map { sanitizeTaskName(it) }
        }

        tasksToReport.forEach { taskName ->
            sender.send(
                RequestedTaskDurationMetric(
                    taskName = taskName,
                    userName = userTag,
                    isInvokedFromIde = metadata.isInvokedFromIde,
                    ramGb = hardwareInfo.ramGb,
                    isCommitChanged = isCommitChanged,
                    durationMs = durationMs,
                )
            )
        }

        executionHistory.writeCurrentState(
            BuildExecutionState(commit = metadata.commitHash)
        )
    }

    private fun isBootstrapTask(taskName: String): Boolean {
        // Bootstrap tasks are expected to be root-level (e.g. `:checkBuildEnvironment`).
        // To avoid accidentally filtering module tasks with the same last segment,
        // we only match strings with no nested task path segments.
        val normalized = taskName.removePrefix(":")
        if (normalized.contains(':')) return false
        return bootstrapRequestedTaskNames.contains(normalized)
    }

    companion object {
        internal fun sanitizeTaskName(name: String): String {
            return name.removePrefix(":").replace(":", "_")
        }
    }
}
