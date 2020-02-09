package com.avito.instrumentation.reservation.request

import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.report.model.TestName
import java.io.Serializable

sealed class Reservation(
    val device: Device,
    val quota: QuotaConfiguration.Data
) : Serializable {

    abstract fun data(tests: List<TestName>): Data

    class StaticReservation(
        device: Device,
        quota: QuotaConfiguration.Data,
        private val count: Int
    ) : Reservation(device, quota) {

        override fun data(tests: List<TestName>): Data =
            Data(
                device = device,
                count = count
            )
    }

    class TestsCountBasedReservation(
        device: Device,
        quota: QuotaConfiguration.Data,
        private val testsPerEmulator: Int,
        private val maximum: Int,
        private val minimum: Int = 1
    ) : Reservation(device, quota) {

        override fun data(tests: List<TestName>): Data {
            val guaranteedTries = quota.minimumSuccessCount + quota.minimumFailedCount

            var calculatedEmulatorsCount = tests.size * guaranteedTries / testsPerEmulator

            if (calculatedEmulatorsCount < minimum) {
                calculatedEmulatorsCount = minimum
            }

            if (calculatedEmulatorsCount > maximum) {
                calculatedEmulatorsCount = maximum
            }

            return Data(
                device = device,
                count = calculatedEmulatorsCount
            )
        }
    }

    data class Data(
        val device: Device,
        val count: Int
    ) : Serializable
}
