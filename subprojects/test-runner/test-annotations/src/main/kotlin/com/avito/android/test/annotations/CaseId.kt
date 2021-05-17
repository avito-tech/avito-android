package com.avito.android.test.annotations

/**
 * Use ExternalId instead
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
public annotation class CaseId(
    val value: Int
)
