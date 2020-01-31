package com.avito.instrumentation.configuration.target.scheduling

import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.DeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.StaticDeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.reservation.request.Reservation
import groovy.lang.Closure
import java.io.Serializable

open class SchedulingConfiguration {

    lateinit var reservation: DeviceReservationConfiguration
    lateinit var quota: QuotaConfiguration

    fun staticDevicesReservation(closure: Closure<StaticDeviceReservationConfiguration>) {
        reservation = getStaticDevicesReservationFromClosure(closure)
    }

    fun testsCountBasedReservation(closure: Closure<TestsBasedDevicesReservationConfiguration>) {
        reservation = getTestCountBasedReservationFromClosure(closure)
    }

    fun quota(closure: Closure<QuotaConfiguration>) {
        quota = QuotaConfiguration()
            .let {
                closure.delegate = it
                closure.call()
                it
            }
    }

    fun validate() {
        reservation
        reservation.validate()

        quota
        quota.validate()
    }

    fun data(): Data {
        val currentReservation = reservation

        return Data(
            reservation = when (currentReservation) {
                is StaticDeviceReservationConfiguration -> Reservation.StaticReservation(
                    device = currentReservation.device,
                    count = currentReservation.count,
                    quota = quota.data()
                )
                is TestsBasedDevicesReservationConfiguration -> Reservation.TestsCountBasedReservation(
                    device = currentReservation.device,
                    quota = quota.data(),
                    testsPerEmulator = currentReservation.testsPerEmulator!!,
                    maximum = currentReservation.maximum!!,
                    minimum = currentReservation.minimum
                )
                else -> throw RuntimeException("Unknown type of reservation")
            }
        )
    }

    private fun getStaticDevicesReservationFromClosure(
        closure: Closure<StaticDeviceReservationConfiguration>
    ): DeviceReservationConfiguration =
        StaticDeviceReservationConfiguration()
            .let {
                closure.delegate = it
                closure.call()
                it
            }
            .apply { validate() }

    private fun getTestCountBasedReservationFromClosure(
        closure: Closure<TestsBasedDevicesReservationConfiguration>
    ): DeviceReservationConfiguration = TestsBasedDevicesReservationConfiguration()
        .let {
            closure.delegate = it
            closure.call()
            it
        }
        .apply {
            validate()
        }

    data class Data(
        val reservation: Reservation
    ) : Serializable
}
