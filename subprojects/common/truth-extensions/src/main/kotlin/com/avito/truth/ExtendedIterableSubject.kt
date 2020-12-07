package com.avito.truth

import com.google.common.collect.FluentIterable
import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.Truth.assertAbout

class ExtendedIterableSubject<T>(
    failureMetadata: FailureMetadata,
    val actual: Iterable<T>
) : Subject(failureMetadata, actual) {

    @Suppress("PROTECTED_CALL_FROM_PUBLIC_INLINE") // for better API
    inline fun <reified T> containsExactlyOne(condition: (T) -> Boolean) {
        val ts = FluentIterable.from(this.actual).filter(T::class.java)
        if (ts.size() != 1) {
            failWithActual("contains exactly one instance of", T::class.simpleName)
        } else {
            val t = ts[0] as T
            if (!condition.invoke(t)) {
                failWithActual(Fact.simpleFact("contains exactly one instance satisfied by condition"))
            }
        }
    }

    companion object {

        inline fun <reified T> iterable() = Factory { metadata, actual: Iterable<T> ->
            ExtendedIterableSubject(metadata, actual)
        }

        inline fun <reified T> assertIterable(iterable: Iterable<T>): ExtendedIterableSubject<T> =
            assertAbout(iterable<T>()).that(iterable)
    }
}
