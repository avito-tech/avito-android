package com.avito.instrumentation.internal

internal class TaskValidatorsFactory {

    fun create(): List<InstrumentationTaskVariantFilter> {
        return listOf(EnvironmentTargetFilter())
    }
}
