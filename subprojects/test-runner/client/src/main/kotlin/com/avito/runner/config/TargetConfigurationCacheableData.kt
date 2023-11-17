package com.avito.runner.config

import com.avito.test.model.DeviceName
import java.io.Serializable

public data class TargetConfigurationCacheableData(
    val name: String,
    val reservation: Reservation,
    val deviceName: DeviceName,
) : Serializable
