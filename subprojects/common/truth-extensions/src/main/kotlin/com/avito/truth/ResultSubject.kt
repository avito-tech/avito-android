package com.avito.truth

import com.avito.android.Result
import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.Truth

class ResultSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: Result<*>
) : Subject(failureMetadata, actual) {

    fun isSuccess() {
        if (actual is Result.Failure) {
            failWithActual(Fact.simpleFact("expected to be Success, but failure with ${actual.throwable}"))
        }
    }

    fun isFailure() {
        if (actual is Result.Success) {
            failWithActual(Fact.simpleFact("expected to be Failure, but was Success"))
        }
    }

    companion object {

        @JvmStatic
        private val RESULT_SUBJECT_FACTORY: Factory<ResultSubject, Result<*>> =
            Factory<ResultSubject, Result<*>> { metadata, actual -> ResultSubject(metadata, actual) }

        @JvmStatic
        fun results(): Factory<ResultSubject, Result<*>> = RESULT_SUBJECT_FACTORY

        @JvmStatic
        fun assertThat(result: Result<*>): ResultSubject =
            Truth.assertAbout(RESULT_SUBJECT_FACTORY).that(result)
    }
}
