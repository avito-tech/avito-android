package com.avito.android.test.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
public annotation class SkipOnSdk(
    vararg val sdk: Int,
    val message: String = ""
)
