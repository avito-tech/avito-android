package com.avito.instrumentation_args

import com.avito.runner.model.InstrumentationParameters

/**
 * Combines args from agp with additional args like report or plugin extension args
 *
 * TODO: Make stronger contract: MBS-7890
 */
public class InstrumentationArgsResolver(
    private val agpArgsProvider: InstrumentationArgsProvider,
    private val extensionArgsProvider: InstrumentationArgsProvider,
    private val additionalArgsProviders: List<InstrumentationArgsProvider>,
) {

    /**
     * These params is set in testInstrumentationRunnerArguments, or by plugin itself
     * Will be resolved early in configuration even if test task is not in execution graph
     * These params will be set as instrumentation args for local run
     */
    public fun resolveLocalRunArgs(): InstrumentationParameters {
        val result = mutableMapOf<String, String>()
        result.putAll(agpArgsProvider.provideInstrumentationArgs())
        additionalArgsProviders.forEach {
            result.putAll(it.provideInstrumentationArgs())
        }
        return InstrumentationParameters(result)
    }

    /**
     * Arguments from all the given sources combined
     * Used by test task
     */
    public fun resolveTestTaskArgs(): InstrumentationParameters {
        return resolveLocalRunArgs()
            .applyParameters(extensionArgsProvider.provideInstrumentationArgs())
    }
}
