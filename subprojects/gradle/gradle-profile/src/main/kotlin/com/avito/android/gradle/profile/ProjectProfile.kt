package com.avito.android.gradle.profile

import org.gradle.util.Path
import java.util.HashMap

public class ProjectProfile(
    public val path: String
) : Operation() {

    private val tasks = HashMap<String, TaskExecution>()

    /**
     * Returns the configuration time of this project.
     */
    public val configurationOperation: ContinuousOperation = ContinuousOperation(path)

    override val description: String
        get() = path

    override val elapsedTime: Long
        get() = getTasks().elapsedTime

    /**
     * Gets the task profiling container for the specified task.
     */
    public fun getTaskProfile(taskPath: String): TaskExecution {
        var result: TaskExecution? = tasks[taskPath]
        if (result == null) {
            result = TaskExecution(Path.path(taskPath))
            tasks[taskPath] = result
        }
        return result
    }

    /**
     * Returns the task executions for this project.
     */
    public fun getTasks(): CompositeOperation<TaskExecution> {
        val taskExecutions = tasks.values.sortedWith(slowestFirst())
        return CompositeOperation(taskExecutions)
    }

    override fun toString(): String {
        return path
    }
}
