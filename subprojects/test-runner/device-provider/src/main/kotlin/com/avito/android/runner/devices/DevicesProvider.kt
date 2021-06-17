package com.avito.android.runner.devices

import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.reservation.DeviceReservation
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.listener.TestListener
import kotlinx.coroutines.CoroutineScope

public interface DevicesProvider : DeviceReservation {

    public suspend fun provideFor(
        reservations: Collection<ReservationData>,
        testListener: TestListener,
        scope: CoroutineScope
    ): DeviceWorkerPool

    public suspend fun releaseDevices()
}
