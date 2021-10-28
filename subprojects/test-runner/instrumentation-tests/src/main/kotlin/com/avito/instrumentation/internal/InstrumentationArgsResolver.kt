package com.avito.instrumentation.internal

import com.android.build.api.dsl.CommonExtension
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.runner.config.InstrumentationParameters
import org.gradle.api.Project

/**
 * Instrumentation args consumed in `parseEnvironment()`
 *
 * TODO: Make stronger contract: MBS-7890
 */
internal class InstrumentationArgsResolver(
    private val extension: InstrumentationTestsPluginExtension,
    private val sentryResolver: SentryResolver,
    private val reportResolver: ReportResolver,
    private val planSlugResolver: PlanSlugResolver,
    private val runIdResolver: RunIdResolver,
    private val buildEnvResolver: BuildEnvResolver,
    private val androidDslInteractor: AndroidDslInteractor,
) {

    private val pluginLevelInstrumentationArgs: MutableMap<String, String> = mutableMapOf()

    /**
     * These params is set in InstrumentationPluginExtension, or available as project properties
     * Will be resolved early in configuration even if test task is not in execution graph
     * These params will be set as instrumentation args for local run
     */
    fun resolvePluginLevelArgs(project: Project, androidExtension: CommonExtension<*, *, *, *>): Map<String, String> {
        if (pluginLevelInstrumentationArgs.isEmpty()) {
            val argsFromDsl = androidDslInteractor.getInstrumentationArgs(androidExtension)

            pluginLevelInstrumentationArgs.resolveArg("planSlug", argsFromDsl) {
                planSlugResolver.generateDefaultPlanSlug(project.path)
            }
            pluginLevelInstrumentationArgs.resolveArg("jobSlug", argsFromDsl) {
                "LocalTests" // todo could be configuration name
            }
            pluginLevelInstrumentationArgs.resolveArg("avito.report.enabled", argsFromDsl) {
                project.getBooleanProperty("avito.report.enabled", default = false).toString()
            }
            pluginLevelInstrumentationArgs.resolveArg("fileStorageUrl", argsFromDsl) {
                reportResolver.getFileStorageUrl()
            }
            pluginLevelInstrumentationArgs.resolveArg("sentryDsn", argsFromDsl) {
                sentryResolver.getSentryDsn().orNull
            }
            pluginLevelInstrumentationArgs.resolveArg("deviceName", argsFromDsl) {
                "local"
            }
            pluginLevelInstrumentationArgs.resolveArg("reportApiUrl", argsFromDsl) {
                reportResolver.getReportApiUrl()
            }
            pluginLevelInstrumentationArgs.resolveArg("reportViewerUrl", argsFromDsl) {
                reportResolver.getReportViewerUrl()
            }

            // runId from dsl will be ignored
            pluginLevelInstrumentationArgs["runId"] = runIdResolver.getLocalRunId().toReportViewerFormat()
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
            .applyParameters(resolveLateArgs())
    }

    /**
     * Postponed resolution of args, which is unavailable locally
     * Local runs of test task without these parameters set is not supported yet
     */
    private fun resolveLateArgs(): Map<String, String> {
        val args = mutableMapOf<String, String>()

        // overwrites runId, because previous value only used for local runs
        args["runId"] = reportResolver.getRunId()
        args["teamcityBuildId"] = buildEnvResolver.getBuildId()
        return args
    }

    private fun MutableMap<String, String>.resolveArg(
        key: String,
        argsFromDsl: Map<String, String>,
        valueFromExtension: () -> String?
    ) {
        val finalValue = if (argsFromDsl.containsKey(key)) {
            val valueFromScript = argsFromDsl[key]
            if (valueFromScript.isNullOrBlank()) {
                valueFromExtension.invoke()
            } else {
                valueFromScript
            }
        } else {
            valueFromExtension.invoke()
        }
        if (!finalValue.isNullOrBlank()) {
            put(key, finalValue)
        }
    }
}
