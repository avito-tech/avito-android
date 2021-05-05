package com.avito.android.build_trace.internal

import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.graph.ShortestPath
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

internal class CriticalPathListener(
    private val serialization: CriticalPathSerialization
) : AbstractBuildEventsListener() {

    private val operations = mutableSetOf<TaskOperation>()

    val criticalPath: List<TaskOperation> by lazy {
        ShortestPath(operations).find()
    }

    // Using a TaskExecutionListener instead of OperationCompletionListener
    //   due to https://github.com/gradle/gradle/issues/15824
    override fun afterExecute(task: Task, state: TaskExecution) {
        val operation = TaskOperation.from(task, state)
        // invert time to find the shortest path as the longest
        val inverted = operation.copy(
            startMs = -operation.startMs,
            finishMs = -operation.finishMs
        )
        operations.add(inverted)
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        serialization.write(criticalPath)
    }

    companion object {

        fun from(project: Project): CriticalPathListener {
            val writer = CriticalPathSerialization(
                report = File(project.projectDir, "outputs/avito/build-trace/critical_path.json")
            )
            return CriticalPathListener(
                writer
            )
        }
    }
}
