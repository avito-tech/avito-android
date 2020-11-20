package com.avito.instrumentation.configuration.target

import com.avito.instrumentation.configuration.InstrumentationParameters
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.reservation.request.Reservation
import groovy.lang.Closure
import org.gradle.api.Action
import java.io.Serializable

open class TargetConfiguration(val name: String) : Serializable {

    lateinit var scheduling: SchedulingConfiguration

    lateinit var deviceName: String

    /**
     * Таргет может считаться отключенным. Такая возможность добавлена для
     * динамической конфигурации. Таргеты могут быть отключены в DSL с помощью
     * проброшенных извне Gradle параметров
     */
    var enabled: Boolean = true

    var instrumentationParams: Map<String, String> = emptyMap()

    fun scheduling(closure: Closure<SchedulingConfiguration>) {
        scheduling(
            Action {
                closure.delegate = it
                closure.call()
            }
        )
    }

    fun scheduling(action: Action<SchedulingConfiguration>) {
        scheduling = SchedulingConfiguration()
            .also {
                action.execute(it)
                it.validate()
            }
    }

    fun data(parentInstrumentationParameters: InstrumentationParameters): Data {

        return Data(
            name = name,
            reservation = scheduling.data().reservation,
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
        require(deviceName.isNotBlank()) { "deviceName must be set" }
    }

    data class Data(
        val name: String,
        val reservation: Reservation,
        val deviceName: String,
        val instrumentationParams: InstrumentationParameters
    ) : Serializable {

        override fun toString(): String = "$name with device name: $deviceName"

        companion object
    }
}
