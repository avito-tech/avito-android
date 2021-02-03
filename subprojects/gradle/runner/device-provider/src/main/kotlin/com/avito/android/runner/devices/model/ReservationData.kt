package com.avito.android.runner.devices.model

import com.avito.instrumentation.reservation.request.Device
import java.io.Serializable

public data class ReservationData(
    val device: Device,
    val count: Int
) : Serializable
