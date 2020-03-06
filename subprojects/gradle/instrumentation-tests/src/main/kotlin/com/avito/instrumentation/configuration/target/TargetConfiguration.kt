package com.avito.instrumentation.configuration.target

import com.avito.instrumentation.configuration.InstrumentationParameters
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.reservation.request.Reservation
import groovy.lang.Closure
import java.io.Serializable

open class TargetConfiguration(val name: String) : Serializable {

    lateinit var scheduling: SchedulingConfiguration
    var rerunScheduling: SchedulingConfiguration? = null

    lateinit var deviceName: String

    /**
     * Таргет может считаться отключенным. Такая возможность добавлена для
     * динамической конфигурации. Таргеты могут быть отключены в DSL с помощью
     * проброшенных извне Gradle параметров
     */
    var enabled: Boolean = true

    var instrumentationParams: Map<String, String> = emptyMap()

    fun scheduling(closure: Closure<SchedulingConfiguration>) {
        scheduling = SchedulingConfiguration()
            .let {
                closure.delegate = it
                closure.call()
                it
            }
    }

    fun rerunScheduling(closure: Closure<SchedulingConfiguration>) {
        rerunScheduling = SchedulingConfiguration()
            .let {
                closure.delegate = it
                closure.call()
                it
            }
    }

    fun data(parentInstrumentationParameters: InstrumentationParameters): Data {

        return Data(
            name = name,
            reservation = scheduling.data().reservation,
            rerunReservation = rerunScheduling?.data()?.reservation ?: scheduling.data().reservation,
            deviceName = deviceName,
            instrumentationParams = parentInstrumentationParameters
                .applyParameters(instrumentationParams)
                .applyParameters(
                    mapOf("deviceName" to deviceName)
                )
        )
    }

    fun validate() {
        scheduling.validate()
        rerunScheduling?.validate()
        require(deviceName.isNotBlank()) { "deviceName must be set" }
    }

    data class Data(
        val name: String,
        val reservation: Reservation,
        val rerunReservation: Reservation,
        val deviceName: String,
        val instrumentationParams: InstrumentationParameters
    ) : Serializable {

        override fun toString(): String = "$name with device name: $deviceName"

        companion object
    }
}
