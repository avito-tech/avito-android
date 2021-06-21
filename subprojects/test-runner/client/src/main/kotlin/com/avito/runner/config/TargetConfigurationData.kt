package com.avito.runner.config

import com.avito.test.model.DeviceName
import java.io.Serializable

public data class TargetConfigurationData(
    val name: String,
    val reservation: Reservation,
    val deviceName: DeviceName,
    val instrumentationParams: InstrumentationParameters
) : Serializable {

    override fun toString(): String = "$name with device name: $deviceName"

    public companion object
}
