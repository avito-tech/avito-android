package org.gradle.kotlin.dsl

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

/**
 * Defines a new object, which will be created when it is required.
 *
 * @see [TaskContainer.register]
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified T : Task> TaskContainer.register(name: String): TaskProvider<T> =
    register(name, T::class.java)


/**
 * Defines and configure a new object, which will be created when it is required.
 *
 * @see [TaskContainer.register]
 */
inline fun <reified T : Task> TaskContainer.register(
    name: String,
    noinline configuration: T.() -> Unit
): TaskProvider<T> =
    register(name, T::class.java, configuration)

/**
 * Defines and configure a new object, which will be created when it is required.
 *
 * @see [TaskContainer.register]
 */
inline fun <reified T : Task> TaskContainer.register(
    name: String,
    vararg constructorArgs: Any,
    noinline configuration: T.() -> Unit
): TaskProvider<T> =
    register(name, T::class.java, *constructorArgs)
        .also { it.configure(configuration) }
