@file:Suppress("UnstableApiUsage")

package org.gradle.kotlin.dsl

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Creates a [Property] that holds values of the given type [T].
 *
 * @see [ObjectFactory.property]
 */
inline fun <reified T> ObjectFactory.property(): Property<T> =
    property(T::class.java)
