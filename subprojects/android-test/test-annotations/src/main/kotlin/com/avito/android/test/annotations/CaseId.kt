package com.avito.android.test.annotations

@Deprecated("Use ExternalId")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class CaseId(
    val value: Int
)
