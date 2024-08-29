package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation_args.InstrumentationArgsProvider

internal class ExtensionInstrumentationArgsProvider(
    private val extension: InstrumentationTestsPluginExtension
) : InstrumentationArgsProvider {

    override fun provideInstrumentationArgs(): Map<String, String> {
        return extension.instrumentationParams
    }
}
