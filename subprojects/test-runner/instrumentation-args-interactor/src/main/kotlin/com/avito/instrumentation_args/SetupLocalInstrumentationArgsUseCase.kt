package com.avito.instrumentation_args

import com.android.build.api.dsl.CommonExtension

public class SetupLocalInstrumentationArgsUseCase(
    private val agpInstrumentationArgsProvider: AgpInstrumentationArgsProvider,
    private val localRunInteractor: LocalRunInteractor,
) {

    public fun setupLocalRunParams(androidExtension: CommonExtension<*, *, *, *, *>) {
        agpInstrumentationArgsProvider.init(androidExtension)
        localRunInteractor.setupLocalRunInstrumentationArgs(androidExtension)
    }
}
