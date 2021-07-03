package com.avito.ci

import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

public sealed class TasksPredicate<T> {

    public abstract fun find(tasks: TaskContainer): T

    public class ByName(
        private val name: String
    ) : TasksPredicate<TaskProvider<Task>>() {

        override fun find(tasks: TaskContainer): TaskProvider<Task> {
            return tasks.named(name)
        }
    }

    public class ByType<T : Task>(
        private val type: Class<T>
    ) : TasksPredicate<TaskCollection<T>>() {

        override fun find(tasks: TaskContainer): TaskCollection<T> {
            return tasks.withType(type)
        }
    }

    public companion object {

        @JvmStatic
        public fun byName(name: String): TasksPredicate<TaskProvider<Task>> = ByName(name)

        @JvmStatic
        public inline fun <reified T : Task> byType(): TasksPredicate<TaskCollection<T>> = ByType(T::class.java)

        @JvmStatic
        public fun <T : Task> byType(type: Class<T>): TasksPredicate<TaskCollection<T>> = ByType(type)
    }
}
