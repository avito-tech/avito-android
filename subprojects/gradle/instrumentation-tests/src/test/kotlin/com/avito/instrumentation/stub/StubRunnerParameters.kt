package com.avito.instrumentation

import com.avito.instrumentation.internal.executing.ExecutionParameters

fun ExecutionParameters.Companion.createStubInstance(
    applicationPackageName: String = "com.test.app",
    applicationTestPackageName: String = "com.test.app.test",
    testRunner: String = "com.test.TestRunner",
    namespace: String = "",
    logcatTags: List<String> = listOf(),
    enableDeviceDebug: Boolean = false
) = ExecutionParameters(
    applicationPackageName = applicationPackageName,
    applicationTestPackageName = applicationTestPackageName,
    testRunner = testRunner,
    namespace = namespace,
    logcatTags = logcatTags,
    enableDeviceDebug = enableDeviceDebug
)
