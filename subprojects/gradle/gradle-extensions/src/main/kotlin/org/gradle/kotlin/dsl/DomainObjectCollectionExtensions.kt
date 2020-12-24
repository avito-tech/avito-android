package org.gradle.kotlin.dsl

import org.gradle.api.DomainObjectCollection

/**
 * Returns a collection containing the objects in this collection of the given type. Equivalent to calling
 * {@code withType(type).all(configureAction)}
 *
 * @param S The type of objects to find.
 * @param configuration The action to execute for each object in the resulting collection.
 * @return The matching objects. Returns an empty collection if there are no such objects
 * in this collection.
 * @see [DomainObjectCollection.withType]
 */
inline fun <reified S : Any> DomainObjectCollection<in S>.withType(noinline configuration: S.() -> Unit) =
    withType(S::class.java, configuration)
