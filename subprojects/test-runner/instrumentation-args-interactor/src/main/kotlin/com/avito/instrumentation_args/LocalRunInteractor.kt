package com.avito.instrumentation_args

import com.android.build.api.dsl.CommonExtension
import com.avito.instrumentation_args.internal.AndroidDslInteractor

/**
 * Local test runs with [AndroidJunitRunner](https://developer.android.com/training/testing/junit-runner)
 * still needs some args to produce reportViewer reports and use logging,
 * see [com.avito.android.runner.environment.TestRunEnvironment]
 */
public class LocalRunInteractor(
    private val instrumentationArgsDumper: InstrumentationArgsDumper,
    private val instrumentationArgsResolver: InstrumentationArgsResolver,
) {

    public fun setupLocalRunInstrumentationArgs(androidExtension: CommonExtension<*, *, *, *, *>) {
        val args = instrumentationArgsResolver.resolveLocalRunArgs()
        AndroidDslInteractor.addInstrumentationArgs(androidExtension, args)
        instrumentationArgsDumper.dumpArgs(AndroidDslInteractor.getInstrumentationArgs(androidExtension))
    }
}
