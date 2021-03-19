package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import java.io.File

interface TestLifecycleListener {

    fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    )

    fun finished(
        artifacts: Result<File>,
        test: TestCase,
        executionNumber: Int
    )
}
