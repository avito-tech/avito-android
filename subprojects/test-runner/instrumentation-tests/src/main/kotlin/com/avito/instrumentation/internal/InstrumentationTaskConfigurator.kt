package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask

internal interface InstrumentationTaskConfigurator {

    fun configure(task: InstrumentationTestsTask)
}
