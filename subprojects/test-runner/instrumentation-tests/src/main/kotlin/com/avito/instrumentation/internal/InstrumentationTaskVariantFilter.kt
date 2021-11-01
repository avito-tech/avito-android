package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.ExecutionEnvironment
import com.avito.instrumentation.configuration.InstrumentationConfiguration

internal interface InstrumentationTaskVariantFilter {

    /**
     * @return true if task variant should be created
     */
    fun filter(configuration: InstrumentationConfiguration, environment: ExecutionEnvironment): Boolean
}
