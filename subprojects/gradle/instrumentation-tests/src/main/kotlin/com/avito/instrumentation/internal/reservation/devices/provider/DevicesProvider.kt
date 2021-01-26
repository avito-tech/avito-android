package com.avito.instrumentation.internal.reservation.devices.provider

import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.reservation.DeviceReservation
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

internal interface DevicesProvider : DeviceReservation {

    fun provideFor(
      reservations: Collection<Reservation.Data>,
      scope: CoroutineScope
    ): ReceiveChannel<Device>

    suspend fun releaseDevices()
}
