package com.avito.ci

import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

public sealed class TasksPredicate<T> {

    abstract fun find(tasks: TaskContainer): T

    class ByName(
        private val name: String
    ) : TasksPredicate<TaskProvider<Task>>() {

        override fun find(tasks: TaskContainer): TaskProvider<Task> {
            return tasks.named(name)
        }
    }

    class ByType<T : Task>(
        private val type: Class<T>
    ) : TasksPredicate<TaskCollection<T>>() {

        override fun find(tasks: TaskContainer): TaskCollection<T> {
            return tasks.withType(type)
        }
    }

    companion object {

        @JvmStatic
        fun byName(name: String): TasksPredicate<TaskProvider<Task>> = ByName(name)

        @JvmStatic
        inline fun <reified T : Task> byType(): TasksPredicate<TaskCollection<T>> = ByType(T::class.java)

        @JvmStatic
        fun <T : Task> byType(type: Class<T>): TasksPredicate<TaskCollection<T>> = ByType(type)
    }
}
