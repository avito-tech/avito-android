package com.avito.kotlin.dsl

import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

/**
 *  Returns TaskProvider for task with specified name or null if there is no that task
 */
public fun TaskContainer.namedOrNull(name: String): TaskProvider<Task>? {
    return try {
        named(name)
    } catch (e: UnknownTaskException) {
        null
    }
}

/**
 *  Returns TaskProvider for task with specified name or null if there is no that task
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Task> TaskContainer.typedNamedOrNull(name: String): TaskProvider<T>? {
    return try {
        named(name) as TaskProvider<T>
    } catch (e: UnknownTaskException) {
        null
    }
}

/**
 *  Returns TaskProvider for task with specified name or fail with UnknownTaskException
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Task> TaskContainer.typedNamed(name: String): TaskProvider<T> {
    return named(name) as TaskProvider<T>
}

public inline fun <reified T> TaskCollection<*>.configureEach(crossinline block: (Task: T) -> Unit) {
    this.configureEach { task ->
        if (task is T) {
            block(task)
        }
    }
}

public inline fun <reified T : Task> TaskContainer.withType(): TaskCollection<T> = withType(T::class.java)
