package com.avito.android.build_trace.internal.critical_path

import com.avito.android.build_trace.extensionName
import com.avito.android.build_trace.internal.TaskDependenciesResolutionResult
import com.avito.android.build_trace.internal.predecessors
import com.avito.android.build_trace.internal.type
import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.composite_exception.CompositeException
import com.avito.graph.OperationsPath
import com.avito.graph.ShortestPath
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import org.gradle.BuildResult
import org.gradle.api.Task
import java.io.PrintWriter
import java.io.StringWriter

internal class CriticalPathListener(
    private val serialization: CriticalPathSerialization,
    loggerFactory: GradleLoggerFactory
) : AbstractBuildEventsListener(), CriticalPathProvider {

    private val logger = loggerFactory.create<CriticalPathListener>()
    private val operations = mutableSetOf<TaskOperation>()

    private val criticalPath: OperationsPath<TaskOperation> by lazy {
        val shortestPath = ShortestPath(operations).find()

        OperationsPath(
            shortestPath.operations.map { it.invertTime() }
        )
    }

    override fun path(): OperationsPath<TaskOperation> {
        return criticalPath
    }

    // Using a TaskExecutionListener instead of OperationCompletionListener
    //   due to https://github.com/gradle/gradle/issues/15824
    override fun afterExecute(task: Task, state: TaskExecution) {
        operations.add(
            taskOperation(task, state)
                .invertTime()
        )
    }

    private fun taskOperation(task: Task, state: TaskExecution): TaskOperation {
        val dependenciesResolution = task.predecessors
        trackDependencyResolutionProblems(task, dependenciesResolution)

        return TaskOperation(
            path = task.path,
            type = task.type.name,
            startMs = state.startTime,
            finishMs = state.finish,
            predecessors = dependenciesResolution.tasks.map { it.path }.toSet()
        )
    }

    private fun trackDependencyResolutionProblems(task: Task, result: TaskDependenciesResolutionResult) {
        if (result.suppressedErrors.isNotEmpty()) {
            val details = result.suppressedErrors.joinToString()
            logger.debug("Ignore dependencies resolution errors for ${task.path}: " + details)
        }
        // TODO: use common:problem for better composability
        if (result is TaskDependenciesResolutionResult.Failed) {
            val message = """Can't find predecessors for task ${task.path}
                Possible solutions: 
                - Disable plugin by $extensionName.enabled extension.
                Caused by:
                ${result.unexpectedError.message}
                
                ${result.unexpectedError.stacktraceAsString()}
                """.trimIndent()
            throw IllegalStateException(message)
        }
    }

    private fun CompositeException.stacktraceAsString(): String {
        val writer = StringWriter()
        printStackTrace(PrintWriter(writer))
        return writer.toString()
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
