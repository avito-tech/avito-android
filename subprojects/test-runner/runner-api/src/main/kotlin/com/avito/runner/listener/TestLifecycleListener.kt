package com.avito.runner.listener

import com.avito.runner.model.TestResult
import com.avito.test.model.TestCase

public interface TestLifecycleListener {

    public fun started(
        test: TestCase,
        deviceId: String,
        executionNumber: Int
    )

    public fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    )
}
