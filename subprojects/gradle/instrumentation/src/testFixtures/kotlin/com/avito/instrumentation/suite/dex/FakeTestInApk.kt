package com.avito.instrumentation.suite.dex

import com.avito.report.model.TestName

fun TestInApk.Companion.createStubInstance(
    className: String = "com.avito.android.test.XXX",
    methodName: String = "test",
    annotations: List<AnnotationData> = emptyList()
) = TestInApk(
    testName = TestName(className, methodName),
    annotations = annotations
)
