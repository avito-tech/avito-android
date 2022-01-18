package com.avito.instrumentation.internal

import com.android.build.api.dsl.CommonExtension
import org.slf4j.Logger

/**
 * Local test runs with [AndroidJunitRunner](https://developer.android.com/training/testing/junit-runner)
 * still needs some args to produce reportViewer reports and use logging,
 * see [com.avito.android.runner.TestRunEnvironment]
 *
 * todo it should be extracted from AvitoTestRunner plugin
 */
internal class LocalRunInteractor(
    private val argsTester: LocalRunArgsChecker,
    private val dslInteractor: AndroidDslInteractor,
    private val logger: Logger
) {

    fun setupLocalRunInstrumentationArgs(androidExtension: CommonExtension<*, *, *, *>, args: Map<String, String>) {
        val filteredArgs = filterNotBlankValues(args) { key ->
            logger.warn("Runner argument '$key' was filtered out for local ui tests run because of a blank value")
        }

        dslInteractor.addInstrumentationArgs(androidExtension, filteredArgs)

        argsTester.dumpArgs(dslInteractor.getInstrumentationArgs(androidExtension))
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
