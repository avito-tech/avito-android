package com.avito.android.test.annotations

/**
 * Marks flaky tests.
 *
 * https://avito-tech.github.io/avito-android/docs/test/flakytests/
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Flaky(
    val reason: String = NO_REASON,
    vararg val onSdks: Int
)

const val NO_REASON = "Unknown reason"
