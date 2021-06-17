package com.avito.android.runner.devices.internal

import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClient
import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.adb.AdbDeviceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.map

internal class KubernetesDevicesProvider(
    private val client: KubernetesReservationClient,
    private val adbDeviceFactory: AdbDeviceFactory,
    private val devicesManager: DevicesManager,
    private val deviceWorkerPoolProvider: DeviceWorkerPoolProvider
) : DevicesProvider {

    override suspend fun provideFor(
        reservations: Collection<ReservationData>,
        testListener: TestListener,
        scope: CoroutineScope
    ): DeviceWorkerPool {
        val claim = client.claim(reservations, scope)
        // TODO parallel device getting
        @Suppress("DEPRECATION")
        val devices = claim.deviceCoordinates.map { coordinate ->
            val adbDeviceParams = devicesManager.findDevice(coordinate.serial)
                .orElseGet {
                    throw IllegalStateException("Can't find device connected adb device ${coordinate.serial}")
                }
            adbDeviceFactory.create(
                coordinate = coordinate,
                adbDeviceParams = adbDeviceParams
            )
        }
        return deviceWorkerPoolProvider.provide(devices, testListener)
    }

    override suspend fun releaseDevice(
        coordinate: DeviceCoordinate,
        scope: CoroutineScope
    ) {
        check(coordinate is DeviceCoordinate.Kubernetes)
        client.remove(coordinate.podName)
    }

    override suspend fun releaseDevices() {
        client.release()
    }
}
