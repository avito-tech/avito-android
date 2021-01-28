package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationParameters
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.stub.reservation.request.createStubInstance

fun TargetConfiguration.Data.Companion.createStubInstance(
    deviceName: String = "functional-24",
    reservation: Reservation = Reservation.StaticReservation(
        device = Device.CloudEmulator.createStubInstance(),
        count = 24,
        quota = QuotaConfiguration.Data(
            retryCount = 0,
            minimumSuccessCount = 1,
            minimumFailedCount = 0
        )
    ),
    instrumentationParams: InstrumentationParameters = InstrumentationParameters()
) = TargetConfiguration.Data(
    name = "${deviceName}Configuration",
    reservation = reservation,
    deviceName = deviceName,
    instrumentationParams = instrumentationParams
)
