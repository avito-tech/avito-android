package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClient
import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDeviceFactory
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map

internal class KubernetesDevicesProvider(
    private val client: KubernetesReservationClient,
    private val adbDeviceFactory: AdbDeviceFactory,
    private val devicesManager: DevicesManager,
    private val deviceWorkerPoolProvider: DeviceWorkerPoolProvider
) : DevicesProvider {

    override suspend fun provideFor(
        reservations: Collection<ReservationData>
    ): DeviceWorkerPool {
        val claim = client.claim(reservations)
        // TODO parallel device getting
        @Suppress("DEPRECATION")
        val devices = claim.deviceCoordinates
            .map { coordinate ->
                createAdbDevice(coordinate)
            }.filterCreatedDevices()

        return deviceWorkerPoolProvider.provide(devices)
    }

    private suspend fun createAdbDevice(coordinate: DeviceCoordinate): Result<AdbDevice> {
        val adbDeviceParams = devicesManager.findDevice(coordinate.serial)
            .orElseGet {
                throw IllegalStateException("Can't find device connected adb device ${coordinate.serial}")
            }
        return adbDeviceFactory.create(
            coordinate = coordinate,
            adbDeviceParams = adbDeviceParams
        ).onFailure { releaseDevice(coordinate) }
    }

    @Suppress("DEPRECATION")
    private fun ReceiveChannel<Result<AdbDevice>>.filterCreatedDevices(): ReceiveChannel<AdbDevice> {
        return filter { it.isSuccess() }.map { it.getOrThrow() }
    }

    override suspend fun releaseDevice(coordinate: DeviceCoordinate) {
        check(coordinate is DeviceCoordinate.Kubernetes)
        client.remove(coordinate.podName)
    }

    override suspend fun releaseReservation(name: String) {
        client.removeDeployment(name)
    }

    override suspend fun releaseDevices() {
        client.release()
    }
}
