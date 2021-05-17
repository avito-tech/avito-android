package com.avito.android

import com.avito.report.model.TestName

fun TestInApk.Companion.createStubInstance(
    className: String = "com.avito.android.test.XXX",
    methodName: String = "test",
    annotations: List<AnnotationData> = emptyList()
) = TestInApk(
    testName = TestName(className, methodName),
    annotations = annotations
)
