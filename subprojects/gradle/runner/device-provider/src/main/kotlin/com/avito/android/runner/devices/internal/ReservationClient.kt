package com.avito.android.runner.devices.internal

import com.avito.android.runner.devices.model.ReservationData
import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

internal interface ReservationClient {

    class ClaimResult(
        val deviceCoordinates: ReceiveChannel<DeviceCoordinate>
    )

    fun claim(
        reservations: Collection<ReservationData>,
        scope: CoroutineScope
    ): ClaimResult

    suspend fun remove(
        podName: String,
        scope: CoroutineScope
    )

    suspend fun release()
}
