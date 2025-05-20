package com.avito.android.bmp.collector

import org.gradle.api.Task
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationDetails
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.TaskDependency
import org.gradle.execution.RunRootBuildWorkBuildOperationType
import org.gradle.internal.operations.BuildOperationCategory
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.BuildOperationMetadata
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent
import org.gradle.internal.taskgraph.CalculateTreeTaskGraphBuildOperationType
import java.time.Instant

@Suppress("UnstableApiUsage")
internal abstract class BuildMetricsCollector : BuildService<BuildMetricsCollector.Params>,
    BuildOperationListener,
    AutoCloseable {

    interface Params : BuildServiceParameters

    private val details: MutableMap<Class<out Any>, Long> = mutableMapOf()
    private val results: MutableMap<Class<out Any>, Long> = mutableMapOf()
    private val metaDatas: MutableMap<BuildOperationMetadata, Long> = mutableMapOf()
    private val operations: MutableMap<OperationIdentifier, BuildOperationDescriptor> = mutableMapOf()

    override fun started(buildOperation: BuildOperationDescriptor, startEvent: OperationStartEvent) {}

    override fun progress(operationIdentifier: OperationIdentifier, progressEvent: OperationProgressEvent) {}

    override fun finished(buildOperation: BuildOperationDescriptor, finishEvent: OperationFinishEvent) {
        val id = buildOperation.id
        if (id != null) {
            operations.put(id, buildOperation)
        }
        val detailsClass = buildOperation.details?.let { it::class.java }
        val resultClass = finishEvent.result?.let { it::class.java }
        when (val details = buildOperation.details) {
            is RunRootBuildWorkBuildOperationType.Details ->
                println("Build start time ${Instant.ofEpochMilli(details.buildStartTime)}")

            is CalculateTreeTaskGraphBuildOperationType.Details -> {
                println("CalculateTreeTaskGraphBuildOperationType.Details = $detailsClass")
                println("Configuration end time ${Instant.ofEpochMilli(finishEvent.endTime)}, details=$detailsClass")
            }
        }
        when (finishEvent.result) {
            is ExecuteTaskBuildOperationType.Result -> {
                val details = buildOperation.details as ExecuteTaskBuildOperationDetails
                val task = details.task
                println(
                    buildString {
                        appendLine("Task ${task.path}")
                        appendLine("predecessors = ${task.predecessors}")
                        appendLine("dependenciesByInputs = ${task.dependenciesByInputs}")
                        appendLine("result ${finishEvent.result}")
                    }

                )
                println(
                    buildString {
                        appendLine("Task executed")
                        appendLine("Task name ${task.taskIdentity.name}")
                        appendLine("Task path ${details.taskPath}")
                        appendLine("Task type ${details.taskClass}")
                        appendLine("Task finished at: ${Instant.ofEpochMilli(finishEvent.endTime)}")
                        val taskGraph = task.project.gradle.taskGraph
                        appendLine("Task predecessors: ${taskGraph.getDependencies(task).map { it.path }}")
                    }
                )
            }
        }
        when (buildOperation.metadata) {
            BuildOperationCategory.RUN_WORK -> println("RUN WORK failure: ${finishEvent.failure}")
        }
        if (resultClass != null) {
            results.put(resultClass, results.getOrDefault(resultClass, 0) + 1)
        }
        metaDatas.put(buildOperation.metadata, metaDatas.getOrDefault(buildOperation.metadata, 0) + 1)
        if (detailsClass != null) {
            details.put(detailsClass, details.getOrDefault(detailsClass, 0) + 1)
        }
    }

    internal fun onBuildResult(result: BuildWorkResult) {
        println("Build failure ${result.failure}")
    }

    override fun close() {
        println("Build metrics close")
        println("Build end ${Instant.now()}")
        println(buildString {
            appendLine("========================")
            appendLine("Details info")
            details.toList().sortedBy { it.second }.forEach {
                appendLine("${it.first}=${it.second}")
            }
            appendLine("========================")
        })
        println(buildString {
            appendLine("========================")
            appendLine("Results info")
            results.toList().sortedBy { it.second }.forEach {
                appendLine("${it.first}=${it.second}")
            }
            appendLine("========================")
        })
        println(buildString {
            appendLine("========================")
            appendLine("Metadata info")
            metaDatas.toList().sortedBy { it.second }.forEach {
                appendLine("${it.first}=${it.second}")
            }
            appendLine("========================")
        })
    }
}

private val Task.dependenciesByInputs: Set<Task>
    get() {
        try {
            val dependenciesByInputs = project.gradle.taskGraph.getDependencies(this)
            return dependenciesByInputs
        } catch (e: Throwable) {
            e.printStackTrace()
            return emptySet()
        }
    }
private val Task.predecessors: Set<Task>
    get() {
        val resolutionResults = listOf(
            taskDependencies.resolveDependencies(this),
            mustRunAfter.resolveDependencies(this),
            shouldRunAfter.resolveDependencies(this)
        )
        val tasks: Set<Task> = resolutionResults.flatten().toSet()

        return tasks
    }

private fun TaskDependency.resolveDependencies(task: Task): Set<Task> =
    try {
        getDependencies(task)
    } catch (e: Throwable) {
        e.printStackTrace()
        emptySet()
    }
