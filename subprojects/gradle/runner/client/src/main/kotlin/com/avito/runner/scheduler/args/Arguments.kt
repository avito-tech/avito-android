package com.avito.runner.scheduler.args

import com.avito.runner.logging.Logger
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.runner.model.TestRunRequest
import kotlinx.coroutines.channels.Channel
import java.io.File

data class Arguments(
    val outputDirectory: File,
    val requests: List<TestRunRequest>,
    val devices: Channel<Serial>,
    val logger: Logger,
    val listener: TestLifecycleListener? = null
)

typealias Serial = String
