package com.avito.runner.scheduler.args

import com.avito.logger.Logger
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.File

data class Arguments(
    val outputDirectory: File,
    val requests: List<TestRunRequest>,
    val devices: ReceiveChannel<Device>,
    val logger: Logger,
    val listener: TestLifecycleListener
)
