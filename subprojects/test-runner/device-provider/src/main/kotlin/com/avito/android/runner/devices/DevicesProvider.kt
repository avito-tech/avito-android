package com.avito.android.runner.devices

import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.reservation.DeviceReservation
import com.avito.runner.service.DeviceWorkerPool

public interface DevicesProvider : DeviceReservation {

    public suspend fun provideFor(
        reservations: Collection<ReservationData>
    ): DeviceWorkerPool

    public suspend fun releaseDevices()
}
