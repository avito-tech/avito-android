package com.avito.android.test.annotations

/**
 * Use ExternalId instead
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class CaseId(
    val value: Int
)
