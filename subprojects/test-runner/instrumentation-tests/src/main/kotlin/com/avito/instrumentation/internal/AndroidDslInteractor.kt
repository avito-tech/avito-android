package com.avito.instrumentation.internal

import com.android.build.api.dsl.CommonExtension

internal object AndroidDslInteractor {

    fun getInstrumentationArgs(extension: CommonExtension<*, *, *, *>): Map<String, String> {
        return extension.defaultConfig.testInstrumentationRunnerArguments
    }

    fun addInstrumentationArgs(extension: CommonExtension<*, *, *, *>, args: Map<String, String>) {
        extension.defaultConfig.testInstrumentationRunnerArguments.putAll(args)
    }
}
