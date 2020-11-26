package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.internal.writer.BuildVerdictWriter
import com.avito.utils.getStackTraceString
import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.util.Path

internal class BuildFailureListener(
    private val graph: TaskExecutionGraph,
    private val logs: Map<Path, LogsTextBuilder>,
    private val writer: BuildVerdictWriter
) : Action<BuildResult> {

    override fun execute(result: BuildResult) {
        result.failure?.apply {
            onFailure(this)
        }
        result.gradle?.removeListener(this)
    }

    private fun onFailure(failure: Throwable) {
        val failedTasks = graph.allTasks
            .filter { it.state.failure != null }

        writer.write(
            BuildVerdict(
                rootError = Error(
                    message = failure.localizedMessage,
                    stackTrace = failure.getStackTraceString()
                ),
                failedTasks = failedTasks.map { task ->
                    FailedTask(
                        name = task.name,
                        projectPath = task.project.path,
                        errorOutput = logs[Path.path(task.path)]?.build() ?: "No error logs",
                        originalError = task.state.failure!!.let { error ->
                            Error(
                                message = error.localizedMessage,
                                stackTrace = error.getStackTraceString()
                            )
                        }
                    )
                }
            )
        )
    }
}
