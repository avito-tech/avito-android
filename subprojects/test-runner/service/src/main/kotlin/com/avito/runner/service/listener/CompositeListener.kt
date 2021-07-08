package com.avito.runner.service.listener

import com.avito.android.Result
import com.avito.runner.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.test.model.TestCase
import java.io.File

public class CompositeListener(
    private val listeners: List<TestListener>
) : TestListener {

    override fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    ) {
        listeners.forEach {
            it.started(
                device = device,
                targetPackage = targetPackage,
                test = test,
                executionNumber = executionNumber
            )
        }
    }

    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int,
        testArtifactsDir: Result<File>
    ) {
        listeners.forEach {
            it.finished(
                device = device,
                test = test,
                targetPackage = targetPackage,
                result = result,
                durationMilliseconds = durationMilliseconds,
                executionNumber = executionNumber,
                testArtifactsDir = testArtifactsDir
            )
        }
    }
}
