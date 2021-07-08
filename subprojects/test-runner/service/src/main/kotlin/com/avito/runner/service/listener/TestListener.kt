package com.avito.runner.service.listener

import com.avito.android.Result
import com.avito.runner.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.test.model.TestCase
import java.io.File

public interface TestListener {

    /**
     * Actual test execution on device is about to start,
     * am instrument will be called right after
     */
    public fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    )

    public fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int,
        testArtifactsDir: Result<File>
    )
}
