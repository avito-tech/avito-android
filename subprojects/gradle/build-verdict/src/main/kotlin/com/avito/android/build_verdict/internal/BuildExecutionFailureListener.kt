package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.span.SpannedStringBuilder
import org.gradle.BuildResult
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.util.Path

internal class BuildExecutionFailureListener(
    private val graph: TaskExecutionGraph,
    private val logs: Map<Path, LogsTextBuilder>,
    private val verdicts: Map<Path, SpannedStringBuilder>,
    private val listener: BuildFailedListener
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

        listener.onFailed(
            BuildVerdict.Execution(
                error = Error.from(failure),
                failedTasks = failedTasks.map { task ->
                    FailedTask(
                        name = task.name,
                        projectPath = task.project.path,
                        errorLogs = logs[Path.path(task.path)]?.build() ?: "No error logs",
                        verdict = verdicts[Path.path(task.path)]?.build(),
                        error = task.state.failure!!.let { error -> Error.from(error) }
                    )
                }
            )
        )
    }
}
