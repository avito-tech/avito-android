package com.avito.runner.config

import com.avito.android.runner.devices.model.ReservationData
import com.avito.instrumentation.reservation.request.Device
import java.io.Serializable

public sealed class Reservation : Serializable {

    public abstract val device: Device
    public abstract val quota: QuotaConfigurationData

    public abstract fun data(testsCount: Int): ReservationData

    public data class StaticReservation(
        override val device: Device,
        override val quota: QuotaConfigurationData,
        private val count: Int
    ) : Reservation() {

        override fun data(testsCount: Int): ReservationData =
            ReservationData(
                device = device,
                count = count
            )
    }

    public data class TestsCountBasedReservation(
        override val device: Device,
        override val quota: QuotaConfigurationData,
        private val testsPerEmulator: Int,
        private val maximum: Int,
        private val minimum: Int = 1
    ) : Reservation() {

        override fun data(testsCount: Int): ReservationData {
            val guaranteedTries = quota.minimumSuccessCount + quota.minimumFailedCount

            var calculatedEmulatorsCount = testsCount * guaranteedTries / testsPerEmulator

            if (calculatedEmulatorsCount < minimum) {
                calculatedEmulatorsCount = minimum
            }

            if (calculatedEmulatorsCount > maximum) {
                calculatedEmulatorsCount = maximum
            }

            return ReservationData(
                device = device,
                count = calculatedEmulatorsCount
            )
        }
    }
}
