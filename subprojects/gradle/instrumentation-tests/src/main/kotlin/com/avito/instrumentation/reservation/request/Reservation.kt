package com.avito.instrumentation.reservation.request

import com.avito.android.runner.devices.model.ReservationData
import com.avito.report.model.TestName
import java.io.Serializable

sealed class Reservation(
    val device: Device,
    val quota: QuotaConfigurationData
) : Serializable {

    abstract fun data(tests: List<TestName>): ReservationData

    class StaticReservation(
        device: Device,
        quota: QuotaConfigurationData,
        private val count: Int
    ) : Reservation(device, quota) {

        override fun data(tests: List<TestName>): ReservationData =
            ReservationData(
                device = device,
                count = count
            )
    }

    class TestsCountBasedReservation(
        device: Device,
        quota: QuotaConfigurationData,
        private val testsPerEmulator: Int,
        private val maximum: Int,
        private val minimum: Int = 1
    ) : Reservation(device, quota) {

        override fun data(tests: List<TestName>): ReservationData {
            val guaranteedTries = quota.minimumSuccessCount + quota.minimumFailedCount

            var calculatedEmulatorsCount = tests.size * guaranteedTries / testsPerEmulator

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
