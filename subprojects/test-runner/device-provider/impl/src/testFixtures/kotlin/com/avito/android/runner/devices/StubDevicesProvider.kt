package com.avito.android.runner.devices

import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.channels.ReceiveChannel

public class StubDevicesProvider(
    private val provider: DeviceWorkerPoolProvider,
    private val devices: ReceiveChannel<Device>
) : DevicesProvider {

    public var isReleased: Boolean = false
        private set

    override suspend fun provideFor(
        reservations: Collection<ReservationData>,
    ): DeviceWorkerPool = provider.provide(devices)

    override suspend fun releaseDevices() {
        isReleased = true
    }

    override suspend fun releaseDevice(coordinate: DeviceCoordinate) {
        TODO("Not yet implemented")
    }
}
