package com.avito.android.runner.devices.internal

import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.channels.ReceiveChannel

internal interface ReservationClient {

    class ClaimResult(
        val deviceCoordinates: ReceiveChannel<DeviceCoordinate>
    )

    suspend fun claim(reservations: Collection<ReservationData>): ClaimResult

    suspend fun remove(
        podName: String
    )

    suspend fun removeDeployment(name: String)

    suspend fun release()
}
