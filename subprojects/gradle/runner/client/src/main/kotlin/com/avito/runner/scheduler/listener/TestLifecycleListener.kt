package com.avito.runner.scheduler.listener

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import org.funktionale.tries.Try
import java.io.File

interface TestLifecycleListener {
    
    fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    )

    fun finished(
        artifacts: Try<File>,
        test: TestCase,
        executionNumber: Int
    )
}
