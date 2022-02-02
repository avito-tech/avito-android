package com.avito.runner.reservation

import com.avito.runner.service.worker.device.DeviceCoordinate

public interface DeviceReservation {

    public suspend fun releaseDevice(coordinate: DeviceCoordinate)
    public suspend fun releaseReservation(name: String)
}
