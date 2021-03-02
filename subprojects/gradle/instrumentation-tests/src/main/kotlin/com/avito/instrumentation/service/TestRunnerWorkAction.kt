package com.avito.instrumentation.service

import com.avito.instrumentation.internal.InstrumentationTestsAction
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

@Suppress("UnstableApiUsage")
public abstract class TestRunnerWorkAction : WorkAction<TestRunnerWorkAction.Params> {

    internal interface Params : WorkParameters {

        val service: Property<TestRunnerService>

        val testRunParams: Property<InstrumentationTestsAction.Params>
    }

    override fun execute() {
        parameters.service.get().runTests(parameters.testRunParams.get())
    }
}
