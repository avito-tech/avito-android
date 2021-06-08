package com.avito.instrumentation.configuration.target

import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.runner.config.InstrumentationParameters
import com.avito.runner.config.TargetConfigurationData
import groovy.lang.Closure
import org.gradle.api.Action
import java.io.Serializable

public open class TargetConfiguration(public val name: String) : Serializable {

    public lateinit var scheduling: SchedulingConfiguration

    public lateinit var deviceName: String

    /**
     * Таргет может считаться отключенным. Такая возможность добавлена для
     * динамической конфигурации. Таргеты могут быть отключены в DSL с помощью
     * проброшенных извне Gradle параметров
     */
    public var enabled: Boolean = true

    public var instrumentationParams: Map<String, String> = emptyMap()

    public fun scheduling(closure: Closure<SchedulingConfiguration>) {
        scheduling {
            closure.delegate = it
            closure.call()
        }
    }

    public fun scheduling(action: Action<SchedulingConfiguration>) {
        scheduling = SchedulingConfiguration()
            .also {
                action.execute(it)
                it.validate()
            }
    }

    public fun data(parentInstrumentationParameters: InstrumentationParameters): TargetConfigurationData {

        return TargetConfigurationData(
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

    public fun validate() {
        scheduling.validate()
        require(deviceName.isNotBlank()) { "deviceName must be set" }
    }
}
