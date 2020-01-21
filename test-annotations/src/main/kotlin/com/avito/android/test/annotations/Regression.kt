package com.avito.android.test.annotations

/**
 * Identifies case as part of Regression suite
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Regression
