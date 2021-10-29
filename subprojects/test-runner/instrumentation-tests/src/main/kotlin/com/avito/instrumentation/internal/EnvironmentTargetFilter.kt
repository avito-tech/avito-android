package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.ExecutionEnvironment
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.LocalAdb
import com.avito.instrumentation.reservation.request.Device

internal class EnvironmentTargetFilter : InstrumentationTaskVariantFilter {

    override fun filter(configuration: InstrumentationConfiguration, environment: ExecutionEnvironment): Boolean {
        if (environment is LocalAdb
            && configuration.targetsContainer.any { it.scheduling.reservation.device is Device.CloudEmulator }
        ) {
            return false
        }

        return true
    }
}
