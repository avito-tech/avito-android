@file:Suppress("DEPRECATION")

package com.avito.android.tech_budget.internal.warnings.task

import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.internal.operations.CurrentBuildOperationRef
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.util.Path
import java.util.concurrent.ConcurrentHashMap

internal interface TaskBuildOperationIdProvider {

    fun getBuildOperationId(taskPath: Path): OperationIdentifier
}

internal object DefaultTaskBuildOperationIdProvider : TaskBuildOperationIdProvider,
    TaskExecutionListener,
    BuildAdapter() {

    private val taskOperationIds: MutableMap<Path, OperationIdentifier> = ConcurrentHashMap()

    override fun getBuildOperationId(taskPath: Path): OperationIdentifier {
        return taskOperationIds[taskPath] ?: error(
            "Can't extract operation id for task: $taskPath. Available ids: ${taskOperationIds.map { it.key }}"
        )
    }

    override fun beforeExecute(task: Task) {
        val id = CurrentBuildOperationRef.instance().id
        if (id != null) {
            val path = Path.path(task.path)
            taskOperationIds.getOrPut(path) { id }
        }
    }

    override fun afterExecute(task: Task, state: TaskState) {
        taskOperationIds.remove(Path.path(task.path))
    }

    override fun buildFinished(result: BuildResult) {
        result.gradle?.removeListener(this)
    }
}
