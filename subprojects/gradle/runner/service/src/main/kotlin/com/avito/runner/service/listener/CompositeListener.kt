package com.avito.runner.service.listener

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device

class CompositeListener(
    val listeners: List<TestListener>
) : TestListener {

    override fun intended(test: TestCase, targetPackage: String, executionNumber: Int) {
        listeners.forEach {
            it.intended(
                test = test,
                targetPackage = targetPackage,
                executionNumber = executionNumber
            )
        }
    }

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
        executionNumber: Int
    ) {
        listeners.forEach {
            it.finished(
                device = device,
                test = test,
                targetPackage = targetPackage,
                result = result,
                durationMilliseconds = durationMilliseconds,
                executionNumber = executionNumber
            )
        }
    }
}
