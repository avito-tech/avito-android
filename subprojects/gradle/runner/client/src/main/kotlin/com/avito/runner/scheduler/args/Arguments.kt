package com.avito.runner.scheduler.args

import com.avito.android.stats.StatsDConfig
import com.avito.logger.LoggerFactory
import com.avito.runner.reservation.DeviceReservation
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.File

class Arguments(
    val outputDirectory: File,
    val buildId: String,
    val instrumentationConfigName: String,
    val requests: List<TestRunRequest>,
    val devices: ReceiveChannel<Device>,
    val loggerFactory: LoggerFactory,
    val listener: TestLifecycleListener,
    val reservation: DeviceReservation,
    val statsDConfig: StatsDConfig
) {

    override fun toString(): String {
        return buildString {
            appendLine(requests)
        }
    }
}
