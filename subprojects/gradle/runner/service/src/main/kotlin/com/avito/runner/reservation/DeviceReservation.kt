package com.avito.runner.reservation

import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.CoroutineScope

interface DeviceReservation {

    suspend fun releaseDevice(coordinate: DeviceCoordinate, scope: CoroutineScope)
}
