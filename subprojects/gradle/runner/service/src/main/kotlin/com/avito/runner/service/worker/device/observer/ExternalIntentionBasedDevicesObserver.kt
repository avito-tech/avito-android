package com.avito.runner.service.worker.device.observer

import com.avito.logger.Logger
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.Serial
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map

class ExternalIntentionBasedDevicesObserver(
    private val devicesManager: DevicesManager,
    private val externalIntentionOfSerials: Channel<Serial>,
    private val logger: Logger
) : DevicesObserver {

    //todo use Flow
    @Suppress("DEPRECATION")
    override suspend fun observeDevices(): ReceiveChannel<Device> =
        externalIntentionOfSerials
            .map { intentionSerial ->
                logger.debug("Intention for serial: $intentionSerial received")

                devicesManager.connectedDevices()
                    .find { it.id == intentionSerial }
                    ?: throw RuntimeException("Can't find device by intentionSerial:$intentionSerial")
            }
}
