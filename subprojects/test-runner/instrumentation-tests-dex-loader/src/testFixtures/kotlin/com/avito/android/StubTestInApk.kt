package com.avito.android

import com.avito.test.model.TestName

public fun TestInApk.Companion.createStubInstance(
    className: String = "com.avito.android.test.XXX",
    methodName: String = "test",
    annotations: List<AnnotationData> = emptyList()
): TestInApk = TestInApk(
    testName = TestName(className, methodName),
    annotations = annotations
)
