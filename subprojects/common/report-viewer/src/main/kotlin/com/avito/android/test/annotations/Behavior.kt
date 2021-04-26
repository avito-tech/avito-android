package com.avito.android.test.annotations

import com.avito.report.model.TestCaseBehavior

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Behavior(val behavior: TestCaseBehavior)
