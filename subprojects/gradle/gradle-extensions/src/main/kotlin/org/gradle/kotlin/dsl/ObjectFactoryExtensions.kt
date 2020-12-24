@file:Suppress("UnstableApiUsage")

package org.gradle.kotlin.dsl

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * Creates a [Property] that holds values of the given type [T].
 *
 * @see [ObjectFactory.property]
 */
inline fun <reified T> ObjectFactory.property(): Property<T> =
    property(T::class.java)

/**
 * Creates a [MapProperty] that holds values of the given key type [K] and value type [V].
 *
 * @see [ObjectFactory.mapProperty]
 */
inline fun <reified K, reified V> ObjectFactory.mapProperty(): MapProperty<K, V> =
    mapProperty(K::class.java, V::class.java)

inline fun <reified T> ObjectFactory.listProperty(): ListProperty<T> =
    listProperty(T::class.java)
