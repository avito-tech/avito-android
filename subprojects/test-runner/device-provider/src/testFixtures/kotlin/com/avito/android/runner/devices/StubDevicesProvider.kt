package com.avito.android.runner.devices

import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

public class StubDevicesProvider(
    private val provider: DeviceWorkerPoolProvider,
    private val devices: ReceiveChannel<Device>
) : DevicesProvider {

    override suspend fun provideFor(
        reservations: Collection<ReservationData>,
        testListener: TestListener,
        scope: CoroutineScope
    ): DeviceWorkerPool = provider.provide(devices, testListener)

    override suspend fun releaseDevices() {
        // do nothing
    }

    override suspend fun releaseDevice(coordinate: DeviceCoordinate, scope: CoroutineScope) {
        TODO("Not yet implemented")
    }
}
