package com.avito.runner.config

import com.avito.android.runner.devices.model.createStubInstance
import com.avito.instrumentation.reservation.request.Device

public fun TargetConfigurationData.Companion.createStubInstance(
    deviceName: String = "functional-24",
    reservation: Reservation = Reservation.StaticReservation(
        device = Device.CloudEmulator.createStubInstance(),
        count = 24,
        quota = QuotaConfigurationData(
            retryCount = 0,
            minimumSuccessCount = 1,
            minimumFailedCount = 0
        )
    ),
    instrumentationParams: InstrumentationParameters = InstrumentationParameters()
): TargetConfigurationData = TargetConfigurationData(
    name = "${deviceName}Configuration",
    reservation = reservation,
    deviceName = deviceName,
    instrumentationParams = instrumentationParams
)
