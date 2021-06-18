package com.avito.runner.reservation

import com.avito.runner.service.worker.device.DeviceCoordinate

interface DeviceReservation {

    suspend fun releaseDevice(coordinate: DeviceCoordinate)
}
