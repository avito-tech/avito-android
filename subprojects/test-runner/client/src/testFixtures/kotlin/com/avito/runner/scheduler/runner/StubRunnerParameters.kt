package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.ExecutionParameters

public fun ExecutionParameters.Companion.createStubInstance(
    applicationPackageName: String = "com.test.app",
    applicationTestPackageName: String = "com.test.app.test",
    testRunner: String = "com.test.TestRunner",
    namespace: String = "",
    logcatTags: List<String> = listOf(),
    enableDeviceDebug: Boolean = false
): ExecutionParameters = ExecutionParameters(
    applicationPackageName = applicationPackageName,
    applicationTestPackageName = applicationTestPackageName,
    testRunner = testRunner,
    namespace = namespace,
    logcatTags = logcatTags,
    enableDeviceDebug = enableDeviceDebug
)
