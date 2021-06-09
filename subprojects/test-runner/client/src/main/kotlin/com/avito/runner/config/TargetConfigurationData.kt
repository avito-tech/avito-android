package com.avito.runner.config

import java.io.Serializable

public data class TargetConfigurationData(
    val name: String,
    val reservation: Reservation,
    val deviceName: String,
    val instrumentationParams: InstrumentationParameters
) : Serializable {

    override fun toString(): String = "$name with device name: $deviceName"

    public companion object
}
