package com.avito.instrumentation.configuration.target.scheduling.reservation

import com.avito.instrumentation.reservation.request.Device
import java.io.Serializable

open class DeviceReservationConfiguration : Serializable {

    lateinit var device: Device

    open fun validate() {
        device
    }
}
