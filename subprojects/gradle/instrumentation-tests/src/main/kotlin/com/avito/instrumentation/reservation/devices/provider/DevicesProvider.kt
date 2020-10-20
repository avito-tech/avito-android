package com.avito.instrumentation.reservation.devices.provider

import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface DevicesProvider {
    fun provideFor(reservations: Collection<Reservation.Data>, scope: CoroutineScope): ReceiveChannel<Device>
    suspend fun releaseDevices()
}
