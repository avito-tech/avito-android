package com.avito.instrumentation.reservation.devices.provider

import com.avito.instrumentation.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.commonLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map

class KubernetesDevicesProvider(
  private val client: KubernetesReservationClient,
  private val adbDevicesManager: AdbDevicesManager,
  private val logger: CILogger,
  private val adb: Adb
) : DevicesProvider {

    override fun provideFor(reservations: Collection<Reservation.Data>, scope: CoroutineScope): ReceiveChannel<Device> {
        val claim = client.claim(reservations, scope)
        // TODO parallel device getting
        return claim.deviceCoordinates.map { coordinate ->
            val adbDeviceParams = adbDevicesManager.findDevice(coordinate.serial)
                .orElseGet { throw IllegalStateException("Can't find device connected adb device ${coordinate.serial}") }
            AdbDevice(
                coordinate = coordinate,
                model = adbDeviceParams.model,
                online = adbDeviceParams.online,
                logger = commonLogger(logger),
                adb = adb
            )
        }
    }

    override suspend fun releaseDevices() {
        client.release()
    }
}
