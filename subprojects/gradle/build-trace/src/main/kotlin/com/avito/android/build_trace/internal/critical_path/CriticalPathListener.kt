package com.avito.android.build_trace.internal.critical_path

import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.graph.ShortestPath
import org.gradle.BuildResult
import org.gradle.api.Task

internal class CriticalPathListener(
    private val serialization: CriticalPathSerialization
) : AbstractBuildEventsListener(), CriticalPathProvider {

    private val operations = mutableSetOf<TaskOperation>()

    private val criticalPath: List<TaskOperation> by lazy {
        ShortestPath(operations)
            .find()
            .map { it.invertTime() }
    }

    override fun path(): List<TaskOperation> {
        return criticalPath
    }

    // Using a TaskExecutionListener instead of OperationCompletionListener
    //   due to https://github.com/gradle/gradle/issues/15824
    override fun afterExecute(task: Task, state: TaskExecution) {
        operations.add(
            TaskOperation
                .from(task, state)
                .invertTime()
        )
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        serialization.write(criticalPath)
    }
}

/**
 * To use the shortest path algorithm as the longest path
 */
private fun TaskOperation.invertTime() = copy(
    startMs = -startMs,
    finishMs = -finishMs
)
