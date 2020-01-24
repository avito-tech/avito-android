package com.avito.android.runner.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class UserAgent(val userAgent: String)
