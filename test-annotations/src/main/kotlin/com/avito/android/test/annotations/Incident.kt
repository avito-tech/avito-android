package com.avito.android.test.annotations

/**
 * Case was based on bug/incident
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Incident
