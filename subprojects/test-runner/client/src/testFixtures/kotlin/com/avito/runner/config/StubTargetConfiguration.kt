package com.avito.runner.config

import com.avito.instrumentation.reservation.request.Device
import com.avito.report.model.DeviceName

public fun TargetConfigurationData.Companion.createStubInstance(
    api: Int = 22,
    model: String = "stub",
    deviceName: DeviceName = DeviceName("functional-24"),
    quota: QuotaConfigurationData = QuotaConfigurationData(
        retryCount = 0,
        minimumSuccessCount = 1,
        minimumFailedCount = 0
    ),
    reservation: Reservation = Reservation.StaticReservation(
        device = Device.MockEmulator.create(api, model),
        count = 24,
        quota = quota
    ),
    instrumentationParams: InstrumentationParameters = InstrumentationParameters()
): TargetConfigurationData = TargetConfigurationData(
    name = "${deviceName}Configuration",
    reservation = reservation,
    deviceName = deviceName,
    instrumentationParams = instrumentationParams
)
