package com.avito.instrumentation.reservation.client

import com.avito.instrumentation.reservation.request.Reservation
import kotlinx.coroutines.channels.SendChannel

interface ReservationClient {
    suspend fun claim(
        reservations: Collection<Reservation.Data>,
        serialsChannel: SendChannel<String>
    )

    suspend fun release(reservations: Collection<Reservation.Data>)
}
