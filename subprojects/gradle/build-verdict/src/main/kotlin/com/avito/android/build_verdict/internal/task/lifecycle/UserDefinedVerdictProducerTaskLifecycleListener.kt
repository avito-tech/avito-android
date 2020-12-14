package com.avito.android.build_verdict.internal.task.lifecycle

import com.avito.android.build_verdict.UserDefinedTaskVerdictProducer
import com.avito.android.build_verdict.internal.LogsTextBuilder
import org.gradle.api.Task
import org.gradle.util.Path

internal class UserDefinedVerdictProducerTaskLifecycleListener(
    private val taskVerdictProducers: Lazy<List<UserDefinedTaskVerdictProducer>>,
    private val verdicts: MutableMap<Path, LogsTextBuilder>
) : TaskLifecycleListener<Task>() {
    override val acceptedTask: Class<in Task> = Task::class.java

    override fun afterFailedExecute(task: Task, error: Throwable) {
        taskVerdictProducers
            .value
            .filter { it.accept(task) }
            .map { it.produce(task) }
            .forEach { verdict ->
                verdicts.getOrPut(Path.path(task.path)) { LogsTextBuilder() }
                    .addLine(verdict)
            }
    }
}
