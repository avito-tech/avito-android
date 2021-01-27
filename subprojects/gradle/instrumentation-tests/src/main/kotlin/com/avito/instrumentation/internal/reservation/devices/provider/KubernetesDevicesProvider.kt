package com.avito.instrumentation.internal.reservation.devices.provider

import com.avito.instrumentation.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map

class KubernetesDevicesProvider(
    private val client: KubernetesReservationClient,
    private val adbDevicesManager: AdbDevicesManager,
    private val loggerFactory: LoggerFactory,
    private val adb: Adb
) : DevicesProvider {

    private val logger = loggerFactory.create<KubernetesDevicesProvider>()

    override fun provideFor(reservations: Collection<Reservation.Data>, scope: CoroutineScope): ReceiveChannel<Device> {
        val claim = client.claim(reservations, scope)
        // TODO parallel device getting
        @Suppress("DEPRECATION")
        return claim.deviceCoordinates.map { coordinate ->
            val adbDeviceParams = adbDevicesManager.findDevice(coordinate.serial)
                .orElseGet {
                    throw IllegalStateException("Can't find device connected adb device ${coordinate.serial}")
                }
            logger.debug("Reserve Device ${coordinate.serial}")
            AdbDevice(
                coordinate = coordinate,
                model = adbDeviceParams.model,
                online = adbDeviceParams.online,
                loggerFactory = loggerFactory,
                adb = adb
            )
        }
    }

    override suspend fun releaseDevice(coordinate: DeviceCoordinate, scope: CoroutineScope) {
        check(coordinate is DeviceCoordinate.Kubernetes)
        client.remove(coordinate.podName, scope)
    }

    override suspend fun releaseDevices() {
        client.release()
    }
}
