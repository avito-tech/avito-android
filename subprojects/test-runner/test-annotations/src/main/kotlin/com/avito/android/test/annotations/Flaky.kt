package com.avito.android.test.annotations

/**
 * Marks flaky tests.
 *
 * https://avito-tech.github.io/avito-android/docs/test/flakytests/
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public annotation class Flaky(
    val reason: String = NO_REASON,
    vararg val onSdks: Int
)

public const val NO_REASON: String = "Unknown reason"
