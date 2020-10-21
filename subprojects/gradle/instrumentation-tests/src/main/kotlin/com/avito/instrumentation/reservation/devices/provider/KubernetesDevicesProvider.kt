package com.avito.instrumentation.reservation.devices.provider

import com.avito.instrumentation.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.logging.CILogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map

class KubernetesDevicesProvider(
  private val client: KubernetesReservationClient,
  private val adbDevicesManager: AdbDevicesManager,
  private val logger: CILogger
) : DevicesProvider {

    override fun provideFor(reservations: Collection<Reservation.Data>, scope: CoroutineScope): ReceiveChannel<Device> {
        val claim = client.claim(reservations, scope)
        // TODO parallel device getting
        return claim.serials.map { serial ->
            adbDevicesManager.findDevice(serial)
                .orElseGet { throw IllegalStateException("Can't find device connected adb device $serial") }
        }
    }

    override suspend fun releaseDevices() {
        client.release()
    }
}
