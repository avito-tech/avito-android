package com.avito.runner.test.mock

import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.observer.DevicesObserver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

class MockDevicesObserver : DevicesObserver {

    private val channel: Channel<Device> =
        Channel(Channel.UNLIMITED)

    override suspend fun observeDevices(): ReceiveChannel<Device> =
        channel

    suspend fun send(device: Device) {
        channel.send(device)
    }
}
