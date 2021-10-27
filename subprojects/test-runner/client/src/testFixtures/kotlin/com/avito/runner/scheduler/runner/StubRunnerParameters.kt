package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.ExecutionParameters

public fun ExecutionParameters.Companion.createStubInstance(
    applicationPackageName: String = "com.test.app",
    applicationTestPackageName: String = "com.test.app.test",
    testRunner: String = "com.test.TestRunner",
    logcatTags: List<String> = listOf()
): ExecutionParameters = ExecutionParameters(
    applicationPackageName = applicationPackageName,
    applicationTestPackageName = applicationTestPackageName,
    testRunner = testRunner,
    logcatTags = logcatTags
)
