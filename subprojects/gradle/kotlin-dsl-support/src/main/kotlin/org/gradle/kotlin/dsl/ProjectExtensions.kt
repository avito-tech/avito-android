/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.kotlin.dsl

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.dsl.ScriptHandler
import kotlin.reflect.KClass

/**
 * Sets the default tasks of this project. These are used when no tasks names are provided when
 * starting the build.
 */
@Suppress("nothing_to_inline")
inline fun Project.defaultTasks(vararg tasks: Task) {
    defaultTasks(*tasks.map { it.name }.toTypedArray())
}

/**
 * Returns the plugin convention or extension of the specified type.
 */
inline fun <reified T : Any> Project.the(): T =
    typeOf<T>().let { type ->
        convention.findByType(type)
            ?: convention.findPlugin(T::class.java)
            ?: convention.getByType(type)
    }

/**
 * Returns the plugin convention or extension of the specified type.
 */
fun <T : Any> Project.the(extensionType: KClass<T>): T =
    convention.findByType(extensionType.java)
        ?: convention.findPlugin(extensionType.java)
        ?: convention.getByType(extensionType.java)

/**
 * Configures the repositories for this project.
 *
 * Executes the given configuration block against the [RepositoryHandler] for this
 * project.
 *
 * @param configuration the configuration block.
 */
fun Project.repositories(configuration: RepositoryHandler.() -> Unit) =
    repositories.configuration()

/**
 * Configures the repositories for the script dependencies.
 */
fun ScriptHandler.repositories(configuration: RepositoryHandler.() -> Unit) =
    repositories.configuration()

/**
 * Creates a container for managing named objects of the specified type.
 *
 * The specified type must have a public constructor which takes the name as a [String] parameter.
 *
 * All objects **MUST** expose their name as a bean property named `name`.
 * The name must be constant for the life of the object.
 *
 * @param T The type of objects for the container to contain.
 * @return The container.
 *
 * @see [Project.container]
 */
inline fun <reified T> Project.container(): NamedDomainObjectContainer<T> =
    container(T::class.java)

/**
 * Creates a container for managing named objects of the specified type.
 *
 * The given factory is used to create object instances.
 *
 * All objects **MUST** expose their name as a bean property named `name`.
 * The name must be constant for the life of the object.
 *
 * @param T The type of objects for the container to contain.
 * @param factory The factory to use to create object instances.
 * @return The container.
 *
 * @see [Project.container]
 */
inline fun <reified T> Project.container(noinline factory: (String) -> T): NamedDomainObjectContainer<T> =
    container(T::class.java, factory)
