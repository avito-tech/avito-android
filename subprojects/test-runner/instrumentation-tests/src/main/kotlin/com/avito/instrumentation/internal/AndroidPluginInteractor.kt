package com.avito.instrumentation.internal

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.avito.logger.LoggerFactory
import com.avito.logger.create

internal class AndroidPluginInteractor(loggerFactory: LoggerFactory) {

    private val logger = loggerFactory.create<AndroidPluginInteractor>()

    fun getInstrumentationArgs(testedExtension: TestedExtension): Map<String, String> {
        return testedExtension.defaultConfig.testInstrumentationRunnerArguments
    }

    fun addInstrumentationArgs(testedExtension: TestedExtension, args: Map<String, String>) {
        val filteredArgs = filterNotBlankValues(args) { key ->
            logger.warn("Runner argument '$key' was filtered out for local ui tests run because of a blank value")
        }
        testedExtension.defaultConfig.testInstrumentationRunnerArguments(filteredArgs)
    }

    fun getTestInstrumentationRunnerOrThrow(defaultConfig: DefaultConfig): String {
        val runner: String = requireNotNull(defaultConfig.testInstrumentationRunner) {
            "testInstrumentationRunner must be set"
        }
        require(runner.isNotBlank()) {
            "testInstrumentationRunner must be set. Current value: $runner"
        }
        return runner
    }

    @Suppress("UNCHECKED_CAST")
    private fun filterNotBlankValues(map: Map<String, Any?>, onFilteredOut: (key: String) -> Unit = {}) =
        map.filter { (key: String, value: Any?) ->
            val shouldBeKept = value?.toString().isNullOrBlank().not()
            if (!shouldBeKept) {
                onFilteredOut.invoke(key)
            }
            shouldBeKept
        } as Map<String, String>
}
