package com.avito.android.build_verdict

import org.gradle.api.Task

class UserDefinedTaskVerdictProducer(
    predicate: TaskPredicate,
    producer: TaskVerdictProducer
) : TaskPredicate by predicate, TaskVerdictProducer by producer

interface TaskPredicate {
    fun accept(task: Task): Boolean

    class ByName(private val name: String) : TaskPredicate {
        override fun accept(task: Task) = task.name == name
    }

    class ByType(private val acceptedClass: Class<in Task>) : TaskPredicate {
        override fun accept(task: Task) = acceptedClass.isInstance(task)
    }
}

interface TaskVerdictProducer {
    fun produce(task: Task): String

    companion object {
        internal inline fun create(crossinline producer: (Task) -> String): TaskVerdictProducer {
            return object : TaskVerdictProducer {
                override fun produce(task: Task) = producer(task)
            }
        }
    }
}
