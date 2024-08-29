package com.avito.instrumentation_args

import com.android.build.api.dsl.CommonExtension
import com.avito.instrumentation_args.internal.AndroidDslInteractor
import com.avito.kotlin.dsl.filterNotBlankValues

public class AgpInstrumentationArgsProvider : InstrumentationArgsProvider {

    private val args: MutableMap<String, String> = mutableMapOf()
    private var initialized = false

    public fun init(androidExtension: CommonExtension<*, *, *, *, *>) {
        args.putAll(filterNotBlankValues(AndroidDslInteractor.getInstrumentationArgs(androidExtension)))
        initialized = true
    }

    public override fun provideInstrumentationArgs(): Map<String, String> {
        require(initialized) {
            "AgpInstrumentationArgsProvider was not initialized"
        }
        return args
    }
}
