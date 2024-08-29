package com.avito.instrumentation_args

public interface InstrumentationArgsProvider {
    public fun provideInstrumentationArgs(): Map<String, String>
}
