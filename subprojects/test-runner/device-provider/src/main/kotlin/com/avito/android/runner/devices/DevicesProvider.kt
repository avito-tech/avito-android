package com.avito.android.runner.devices

import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.reservation.DeviceReservation
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

public interface DevicesProvider : DeviceReservation {

    public suspend fun provideFor(
        reservations: Collection<ReservationData>,
        scope: CoroutineScope
    ): ReceiveChannel<Device>

    public suspend fun releaseDevices()
}
