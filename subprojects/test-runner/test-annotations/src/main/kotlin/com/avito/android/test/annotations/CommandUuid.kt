package com.avito.android.test.annotations

/**
 * A team that owns the test and fixes its failures.
 *
 * Value is a team peopleUuid from TMS, a test has exactly one owner.
 * Annotate either a class or a method: the same annotation in both places fails test suite loading,
 * see ClassAndMethodDuplicateAnnotationCheck.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
public annotation class CommandUuid(
    val value: String,
)
