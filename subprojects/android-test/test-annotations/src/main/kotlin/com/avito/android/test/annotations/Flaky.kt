package com.avito.android.test.annotations

/**
 * Marks flaky tests.
 *
 * For more information read [this document](https://avito-tech.github.io/avito-android/docs/test/flakytests/).
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Flaky(val reason: String = NO_REASON)

private const val NO_REASON = ""