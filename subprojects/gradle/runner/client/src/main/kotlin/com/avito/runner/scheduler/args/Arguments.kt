package com.avito.runner.scheduler.args

import com.avito.logger.Logger
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.worker.device.Serial
import kotlinx.coroutines.channels.Channel
import java.io.File

data class Arguments(
    val outputDirectory: File,
    val requests: List<TestRunRequest>,
    val devices: Channel<Serial>,
    val logger: Logger,
    val listener: TestLifecycleListener? = null
)
