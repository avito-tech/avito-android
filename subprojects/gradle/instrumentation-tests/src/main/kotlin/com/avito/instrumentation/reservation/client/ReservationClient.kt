package com.avito.instrumentation.reservation.client

import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.worker.device.Serial
import kotlinx.coroutines.channels.SendChannel

interface ReservationClient {
    suspend fun claim(
        reservations: Collection<Reservation.Data>,
        serialsChannel: SendChannel<Serial>,
        reservationDeployments: SendChannel<String>
    )

    suspend fun release(reservationDeployments: Collection<String>)
}
