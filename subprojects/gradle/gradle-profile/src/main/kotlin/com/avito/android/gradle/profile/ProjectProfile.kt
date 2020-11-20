package com.avito.android.gradle.profile

import org.gradle.util.CollectionUtils
import java.util.HashMap

class ProjectProfile(
    val path: String
) : Operation() {

    private val tasks = HashMap<String, TaskExecution>()

    /**
     * Returns the configuration time of this project.
     */
    val configurationOperation: ContinuousOperation = ContinuousOperation(path)

    override val elapsedTime: Long
        get() = getTasks().elapsedTime

    /**
     * Gets the task profiling container for the specified task.
     */
    fun getTaskProfile(taskPath: String): TaskExecution {
        var result: TaskExecution? = tasks[taskPath]
        if (result == null) {
            result = TaskExecution(taskPath)
            tasks[taskPath] = result
        }
        return result
    }

    /**
     * Returns the task executions for this project.
     */
    fun getTasks(): CompositeOperation<TaskExecution> {
        val taskExecutions = CollectionUtils.sort(tasks.values, Operation.slowestFirst())
        return CompositeOperation(taskExecutions)
    }

    override fun toString(): String {
        return path
    }

    override val description: String
        get() = path
}
