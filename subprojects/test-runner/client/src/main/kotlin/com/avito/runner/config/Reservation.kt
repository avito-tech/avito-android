package com.avito.runner.config

import com.avito.android.runner.devices.model.ReservationData
import com.avito.instrumentation.reservation.request.Device
import java.io.Serializable

public sealed class Reservation(
    public val device: Device,
    public val quota: QuotaConfigurationData
) : Serializable {

    public abstract fun data(testsCount: Int): ReservationData

    public class StaticReservation(
        device: Device,
        quota: QuotaConfigurationData,
        private val count: Int
    ) : Reservation(device, quota) {

        override fun data(testsCount: Int): ReservationData =
            ReservationData(
                device = device,
                count = count
            )
    }

    public class TestsCountBasedReservation(
        device: Device,
        quota: QuotaConfigurationData,
        private val testsPerEmulator: Int,
        private val maximum: Int,
        private val minimum: Int = 1
    ) : Reservation(device, quota) {

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
