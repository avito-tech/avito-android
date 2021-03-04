package com.avito.instrumentation.service

import com.avito.instrumentation.internal.InstrumentationTestsAction
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

@Suppress("UnstableApiUsage")
public abstract class TestRunnerService : BuildService<TestRunnerService.Params>, AutoCloseable {

    public interface Params : BuildServiceParameters {
        // todo
    }

    internal fun runTests(params: InstrumentationTestsAction.Params) {
        InstrumentationTestsAction(params).run()
    }

    override fun close() {
    }
}
