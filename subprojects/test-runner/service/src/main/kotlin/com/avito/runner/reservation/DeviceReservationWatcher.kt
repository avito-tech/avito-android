package com.avito.runner.reservation

import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.channels.ReceiveChannel

public interface DeviceReservationWatcher {

    public suspend fun watch(deviceSignals: ReceiveChannel<Device.Signal>)

    public companion object {

        public fun create(reservation: DeviceReservation): DeviceReservationWatcher =
            DeviceReservationWatcherImpl(reservation)
    }
}
