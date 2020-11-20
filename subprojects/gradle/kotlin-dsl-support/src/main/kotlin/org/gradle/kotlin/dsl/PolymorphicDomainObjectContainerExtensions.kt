package org.gradle.kotlin.dsl

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.PolymorphicDomainObjectContainer

/**
 * Defines a new object, which will be created when it is required.
 *
 * @see [PolymorphicDomainObjectContainer.register]
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified T : Any> PolymorphicDomainObjectContainer<in T>.register(name: String): NamedDomainObjectProvider<T> =
    register(name, T::class.java)

/**
 * Defines and configure a new object, which will be created when it is required.
 *
 * @see [PolymorphicDomainObjectContainer.register]
 */
inline fun <reified T : Any> PolymorphicDomainObjectContainer<in T>.register(
    name: String,
    noinline configuration: T.() -> Unit
): NamedDomainObjectProvider<T> =
    register(name, T::class.java, configuration)
