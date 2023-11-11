package com.avito.instrumentation.internal

import com.android.build.api.dsl.CommonExtension
import com.avito.android.plugins.configuration.RunIdResolver
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation.configuration.report.ReportConfig
import com.avito.kotlin.dsl.filterNotBlankValues
import com.avito.runner.config.InstrumentationParameters

/**
 * Instrumentation args consumed in `parseEnvironment()`
 *
 * TODO: Make stronger contract: MBS-7890
 */
internal class InstrumentationArgsResolver(
    private val extension: InstrumentationTestsPluginExtension,
    private val reportResolver: ReportResolver,
    private val runIdResolver: RunIdResolver,
    private val androidDslInteractor: AndroidDslInteractor,
) {

    private val pluginLevelInstrumentationArgs: MutableMap<String, String> = mutableMapOf()

    /**
     * These params is set in InstrumentationPluginExtension, or available as project properties
     * Will be resolved early in configuration even if test task is not in execution graph
     * These params will be set as instrumentation args for local run
     */
    fun resolvePluginLevelArgs(androidExtension: CommonExtension<*, *, *, *>): Map<String, String> {
        val argsFromDsl = mutableMapOf<String, String>()

        argsFromDsl.putAll(filterNotBlankValues(androidDslInteractor.getInstrumentationArgs(androidExtension)))
        // put everything in args 'as is'
        pluginLevelInstrumentationArgs.putAll(argsFromDsl)
        // override report args
        when (val report = reportResolver.getReport() ?: ReportConfig.NoOp) {
            ReportConfig.NoOp -> pluginLevelInstrumentationArgs["avito.report.transport"] = "noop"
            is ReportConfig.ReportViewer.SendFromDevice -> {
                pluginLevelInstrumentationArgs["avito.report.transport"] = "backend"
                pluginLevelInstrumentationArgs["planSlug"] = report.planSlug
                pluginLevelInstrumentationArgs["jobSlug"] = report.jobSlug
                pluginLevelInstrumentationArgs["runId"] = runIdResolver.getRunId().toReportViewerFormat()
                pluginLevelInstrumentationArgs["fileStorageUrl"] = report.fileStorageUrl
                pluginLevelInstrumentationArgs["reportViewerUrl"] = report.reportViewerUrl
                pluginLevelInstrumentationArgs["reportApiUrl"] = report.reportApiUrl
                pluginLevelInstrumentationArgs["deviceName"] = "local"
            }
            is ReportConfig.ReportViewer.SendFromRunner -> {
                pluginLevelInstrumentationArgs["avito.report.transport"] = "legacy"
                pluginLevelInstrumentationArgs["fileStorageUrl"] = report.fileStorageUrl
            }
        }

        return pluginLevelInstrumentationArgs
    }

    fun getInstrumentationArgsForTestTask(): InstrumentationParameters {
        require(pluginLevelInstrumentationArgs.isNotEmpty()) {
            "Error: pluginLevelInstrumentationArgs is empty\n" +
                "resolvePluginLevelArgs() should be called before\n" +
                "it is that way, because new AGP API doesn't have dsl values available in onVariants API\n" +
                "so we should extract and store it somewhere"
        }

        return InstrumentationParameters()
            .applyParameters(pluginLevelInstrumentationArgs)
            .applyParameters(extension.instrumentationParams)
    }
}
