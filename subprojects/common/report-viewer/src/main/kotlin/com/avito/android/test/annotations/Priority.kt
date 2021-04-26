package com.avito.android.test.annotations

import com.avito.report.model.TestCasePriority

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Priority(val priority: TestCasePriority)
