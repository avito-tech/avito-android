package com.avito.instrumentation.configuration.target.scheduling.reservation

import com.avito.instrumentation.reservation.request.Device
import java.io.Serializable

public open class DeviceReservationConfiguration : Serializable {

    public lateinit var device: Device

    public open fun validate() {
        device
    }
}
