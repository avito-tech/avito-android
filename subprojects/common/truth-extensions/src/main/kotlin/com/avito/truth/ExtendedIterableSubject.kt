package com.avito.truth

import com.google.common.collect.FluentIterable
import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.Truth.assertAbout

public class ExtendedIterableSubject<T>(
    failureMetadata: FailureMetadata,
    private val actual: Iterable<T>
) : Subject(failureMetadata, actual) {

    public fun <T> containsExactlyOne(clazz: Class<T>, condition: (T) -> Boolean) {
        val ts = FluentIterable.from(this.actual).filter(clazz)
        if (ts.size() != 1) {
            failWithActual("contains exactly one instance of", clazz.simpleName)
        } else {
            val t = ts[0] as T
            if (!condition.invoke(t)) {
                failWithActual(Fact.simpleFact("contains exactly one instance satisfied by condition"))
            }
        }
    }

    public companion object {

        public inline fun <reified T> iterable(): Factory<ExtendedIterableSubject<T>, Iterable<T>> =
            Factory { metadata, actual: Iterable<T> ->
                ExtendedIterableSubject(metadata, actual)
            }

        public inline fun <reified T> assertIterable(iterable: Iterable<T>): ExtendedIterableSubject<T> =
            assertAbout(iterable<T>()).that(iterable)
    }
}
