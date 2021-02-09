package com.avito.instrumentation.configuration.target.scheduling

import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.DeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.StaticDeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.reservation.request.Reservation
import groovy.lang.Closure
import org.gradle.api.Action
import java.io.Serializable

public open class SchedulingConfiguration {

    public lateinit var reservation: DeviceReservationConfiguration
    public lateinit var quota: QuotaConfiguration

    public fun staticDevicesReservation(closure: Closure<StaticDeviceReservationConfiguration>) {
        staticDevicesReservation(
            Action {
                closure.delegate = it
                closure.call()
            }
        )
    }

    public fun testsCountBasedReservation(closure: Closure<TestsBasedDevicesReservationConfiguration>) {
        testsCountBasedReservation(
            Action {
                closure.delegate = it
                closure.call()
            }
        )
    }

    public fun staticDevicesReservation(action: Action<StaticDeviceReservationConfiguration>) {
        reservation = StaticDeviceReservationConfiguration().also {
            action.execute(it)
            it.validate()
        }
    }

    public fun testsCountBasedReservation(action: Action<TestsBasedDevicesReservationConfiguration>) {
        reservation = TestsBasedDevicesReservationConfiguration().also {
            action.execute(it)
            it.validate()
        }
    }

    public fun quota(closure: Closure<QuotaConfiguration>) {
        quota(
            Action {
                closure.delegate = it
                closure.call()
            }
        )
    }

    public fun quota(action: Action<QuotaConfiguration>) {
        quota = QuotaConfiguration()
            .also {
                action.execute(it)
                it.validate()
            }
    }

    public fun validate() {
        reservation
        reservation.validate()

        quota
        quota.validate()
    }

    public fun data(): Data {
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

    public data class Data(
        val reservation: Reservation
    ) : Serializable
}
