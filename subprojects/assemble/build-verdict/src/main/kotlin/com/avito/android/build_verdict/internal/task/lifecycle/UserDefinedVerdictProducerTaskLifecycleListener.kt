package com.avito.android.build_verdict.internal.task.lifecycle

import com.avito.android.build_verdict.UserDefinedTaskVerdictProducer
import com.avito.android.build_verdict.span.SpannedStringBuilder
import org.gradle.api.Task
import org.gradle.util.Path

internal class UserDefinedVerdictProducerTaskLifecycleListener(
    private val taskVerdictProducers: Lazy<List<UserDefinedTaskVerdictProducer>>,
    private val verdicts: MutableMap<Path, SpannedStringBuilder>
) : TaskLifecycleListener<Task>() {
    override val acceptedTask: Class<in Task> = Task::class.java

    override fun afterFailedExecute(task: Task, error: Throwable) {
        val builder = verdicts.getOrPut(Path.path(task.path)) { SpannedStringBuilder() }
        builder.addLines(
            newLines = taskVerdictProducers
                .value
                .filter { it.accept(task) }
                .map { it.produce(task) }
        )
    }
}
