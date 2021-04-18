package com.avito.truth

import com.avito.android.Result
import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.Truth

class ResultSubject<T> private constructor(
    private val failureMetadata: FailureMetadata,
    private val actual: Result<T>
) : Subject(failureMetadata, actual) {

    fun isSuccess(): SuccessSubject<T> {
        when (actual) {
            is Result.Failure -> {
                failWithActual(Fact.simpleFact("expected to be Success, but failure with ${actual.throwable}"))
                throw IllegalStateException("Can't be here")
            }
            is Result.Success ->
                return SuccessSubject(failureMetadata, actual.value)
        }
    }

    fun isFailure(): FailureSubject {
        when (actual) {
            is Result.Success -> {
                failWithActual(Fact.simpleFact("expected to be Failure, but was Success"))
                throw IllegalStateException("Can't be here")
            }
            is Result.Failure ->
                return FailureSubject(failureMetadata, actual.throwable)
        }
    }

    class SuccessSubject<T> internal constructor(
        failureMetadata: FailureMetadata,
        private val actual: T
    ) : Subject(failureMetadata, actual) {

        fun withValue(body: (T) -> Unit) {
            body(actual)
        }
    }

    class FailureSubject internal constructor(
        failureMetadata: FailureMetadata,
        private val actual: Throwable
    ) : Subject(failureMetadata, actual) {

        fun withThrowable(body: (Throwable) -> Unit) {
            body(actual)
        }
    }

    companion object {

        @JvmStatic
        private val RESULT_SUBJECT_FACTORY: Factory<ResultSubject<*>, Result<*>> =
            Factory<ResultSubject<*>, Result<*>> { metadata, actual -> ResultSubject(metadata, actual) }

        @JvmStatic
        fun results(): Factory<ResultSubject<*>, Result<*>> = RESULT_SUBJECT_FACTORY

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> assertThat(result: Result<T>): ResultSubject<T> =
            Truth.assertAbout(RESULT_SUBJECT_FACTORY).that(result) as ResultSubject<T>
    }
}
