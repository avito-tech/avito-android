package org.gradle.kotlin.dsl

import org.gradle.api.NamedDomainObjectProvider

/**
 * Allows a [NamedDomainObjectProvider] to be configured via invocation syntax.
 *
 * ```kotlin
 * val rebuild by tasks.registering
 * rebuild { // rebuild.configure {
 *   dependsOn("clean")
 * }
 * ```
 */
public operator fun <T> NamedDomainObjectProvider<T>.invoke(action: T.() -> Unit): Unit = configure(action)
