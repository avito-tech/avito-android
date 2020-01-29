package com.avito.android.test.annotations

@Deprecated("Use FeatureId")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Feature(
    val value: Array<String>
)
