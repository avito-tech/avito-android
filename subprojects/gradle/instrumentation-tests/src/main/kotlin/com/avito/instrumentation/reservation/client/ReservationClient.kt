package com.avito.instrumentation.reservation.client

import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.worker.device.Serial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface ReservationClient {

    class ClaimResult(
        val serials: ReceiveChannel<Serial>
    )

    fun claim(
        reservations: Collection<Reservation.Data>,
        scope: CoroutineScope
    ): ClaimResult

    suspend fun release()
}
