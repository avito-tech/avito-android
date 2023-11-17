package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask

internal class BuildCacheConfigurator(
    private val buildCacheEnabled: Boolean,
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        task.outputs.upToDateWhen { buildCacheEnabled }
    }
}
