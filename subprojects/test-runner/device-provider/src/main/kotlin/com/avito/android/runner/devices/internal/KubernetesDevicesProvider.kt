package com.avito.android.runner.devices.internal

import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClient
import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.adb.AdbDeviceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map

internal class KubernetesDevicesProvider(
    private val client: KubernetesReservationClient,
    private val adbDeviceFactory: AdbDeviceFactory,
    private val devicesManager: DevicesManager
) : DevicesProvider {

    override suspend fun provideFor(
        reservations: Collection<ReservationData>,
        scope: CoroutineScope
    ): ReceiveChannel<Device> {
        val claim = client.claim(reservations, scope)
        // TODO parallel device getting
        @Suppress("DEPRECATION")
        return claim.deviceCoordinates.map { coordinate ->
            val adbDeviceParams = devicesManager.findDevice(coordinate.serial)
                .orElseGet {
                    throw IllegalStateException("Can't find device connected adb device ${coordinate.serial}")
                }
            adbDeviceFactory.create(
                coordinate = coordinate,
                adbDeviceParams = adbDeviceParams
            )
        }
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
