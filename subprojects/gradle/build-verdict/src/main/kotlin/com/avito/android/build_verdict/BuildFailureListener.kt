package com.avito.android.build_verdict

import com.avito.utils.logging.CILogger
import com.google.gson.Gson
import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.Directory
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

internal class BuildFailureListener(
    private val graph: TaskExecutionGraph,
    private val buildVerdictDir: Directory,
    private val logs: Map<TaskPath, StringBuilder>,
    private val ciLogger: CILogger,
    private val gson: Gson
) : Action<BuildResult> {

    override fun execute(result: BuildResult) {
        result.failure?.apply {
            onFailure(this)
        }
    }

    private fun onFailure(failure: Throwable) {
        val failedTasks = graph.allTasks
            .filter { it.state.failure != null }
        val verdict = gson.toJson(
            BuildVerdict(
                rootError = GsonableError(
                    message = failure.localizedMessage,
                    stackTrace = failure.stringStackTrace
                ),
                failedTasks = failedTasks.map { task ->
                    FailedTask(
                        name = task.name,
                        projectPath = task.project.path,
                        errorOutput = logs[TaskPath(task.path)].toString(),
                        originalError = task.state.failure!!.run {
                            GsonableError(
                                message = localizedMessage,
                                stackTrace = stringStackTrace
                            )
                        }
                    )
                }
            )
        )
        val dir = buildVerdictDir.asFile.apply { mkdirs() }
        val file = File(dir, buildVerdictFileName)
        file.createNewFile()
        file.writeText(
            verdict
        )
        ciLogger.warn(
            "Build failed. You can find details at $file"
        )
    }


    private val Throwable.stringStackTrace: String
        get() {
            val stringWriter = StringWriter()
            printStackTrace(PrintWriter(stringWriter))
            return stringWriter.toString()
        }

    companion object {
        internal val buildVerdictFileName = "build-verdict.json"
    }
}
