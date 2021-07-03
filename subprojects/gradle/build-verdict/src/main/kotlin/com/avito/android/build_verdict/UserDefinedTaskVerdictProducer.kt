package com.avito.android.build_verdict

import com.avito.android.build_verdict.span.SpannedString
import org.gradle.api.Task

internal class UserDefinedTaskVerdictProducer(
    predicate: TaskPredicate,
    producer: TaskVerdictProducer
) : TaskPredicate by predicate, TaskVerdictProducer by producer

internal interface TaskPredicate {

    fun accept(task: Task): Boolean

    class ByName(private val name: String) : TaskPredicate {
        override fun accept(task: Task) = task.name == name
    }

    class ByType(private val acceptedClass: Class<in Task>) : TaskPredicate {
        override fun accept(task: Task) = acceptedClass.isInstance(task)
    }
}

public interface TaskVerdictProducer {

    public fun produce(task: Task): SpannedString

    public companion object {

        public inline fun create(crossinline producer: (Task) -> SpannedString): TaskVerdictProducer {
            return object : TaskVerdictProducer {
                override fun produce(task: Task) = producer(task)
            }
        }
    }
}
