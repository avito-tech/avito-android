package com.avito.android.test.annotations

import com.avito.report.model.TestKind

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Kind(val value: TestKind)
