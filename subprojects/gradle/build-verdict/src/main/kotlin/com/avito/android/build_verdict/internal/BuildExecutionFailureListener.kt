package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.internal.writer.BuildVerdictWriter
import org.gradle.BuildResult
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.util.Path

internal class BuildExecutionFailureListener(
    private val graph: TaskExecutionGraph,
    private val logs: Map<Path, LogsTextBuilder>,
    private val writer: BuildVerdictWriter
) : BaseBuildListener() {

    override fun buildFinished(result: BuildResult) {
        result.failure?.apply {
            onFailure(this)
        }
        result.gradle?.removeListener(this)
    }

    private fun onFailure(failure: Throwable) {
        val failedTasks = graph.allTasks
            .filter { it.state.failure != null }

        writer.write(
            BuildVerdict.Execution(
                error = Error.from(failure),
                failedTasks = failedTasks.map { task ->
                    FailedTask(
                        name = task.name,
                        projectPath = task.project.path,
                        errorOutput = logs[Path.path(task.path)]?.build() ?: "No error logs",
                        error = task.state.failure!!.let { error -> Error.from(error) }
                    )
                }
            )
        )
    }
}
