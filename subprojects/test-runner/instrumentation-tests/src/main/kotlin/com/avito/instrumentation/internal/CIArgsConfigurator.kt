package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask

internal class CIArgsConfigurator(
    private val buildEnvResolver: BuildEnvResolver
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        task.buildId.set(buildEnvResolver.getBuildId())
        task.buildType.set(buildEnvResolver.getBuildType())
    }
}
