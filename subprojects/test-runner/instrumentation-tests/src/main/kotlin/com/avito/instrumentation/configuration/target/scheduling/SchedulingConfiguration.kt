package com.avito.instrumentation.configuration.target.scheduling

import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.DeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.StaticDeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import groovy.lang.Closure
import org.gradle.api.Action

public open class SchedulingConfiguration {

    public lateinit var reservation: DeviceReservationConfiguration
    public lateinit var quota: QuotaConfiguration

    public fun staticDevicesReservation(closure: Closure<StaticDeviceReservationConfiguration>) {
        staticDevicesReservation {
            closure.delegate = it
            closure.call()
        }
    }

    public fun testsCountBasedReservation(closure: Closure<TestsBasedDevicesReservationConfiguration>) {
        testsCountBasedReservation {
            closure.delegate = it
            closure.call()
        }
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
        quota {
            closure.delegate = it
            closure.call()
        }
    }

    public fun quota(action: Action<QuotaConfiguration>) {
        quota = QuotaConfiguration()
            .also {
                action.execute(it)
                it.validate()
            }
    }
}
