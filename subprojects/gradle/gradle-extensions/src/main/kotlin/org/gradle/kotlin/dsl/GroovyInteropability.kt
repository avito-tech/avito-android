package org.gradle.kotlin.dslx

import groovy.lang.Closure
import org.gradle.kotlin.dsl.support.uncheckedCast

/**
 * Adapts a Kotlin function to a Groovy [Closure] that operates on the
 * configured Closure delegate.
 *
 * For lazy set [Closure.delegate], [Closure.owner]
 *
 * @param T the expected type of the delegate argument to the closure.
 * @param action the function to be adapted.
 */
public fun <T> noOwnerClosureOf(action: T.() -> Unit): Closure<T> =
    object : Closure<T>(null, null) {
        @Suppress("unused") // to be called dynamically by Groovy
        fun doCall() = uncheckedCast<T>(delegate).action()
    }

/**
 * Adapts a Kotlin function to a single argument Groovy [Closure].
 *
 * @param T the expected type of the single argument to the closure.
 * @param action the function to be adapted.
 *
 * @see [KotlinClosure1]
 */
public fun <T> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
    KotlinClosure1(action, this, this)

/**
 * Adapts an unary Kotlin function to an unary Groovy [Closure].
 *
 * @param T the type of the single argument to the closure.
 * @param V the return type.
 * @param function the function to be adapted.
 * @param owner optional owner of the Closure.
 * @param thisObject optional _this Object_ of the Closure.
 *
 * @see [Closure]
 */
public class KotlinClosure1<in T : Any?, V : Any>(
    public val function: T.() -> V?,
    owner: Any? = null,
    thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {

    @Suppress("unused") // to be called dynamically by Groovy
    public fun doCall(it: T): V? = it.function()
}
