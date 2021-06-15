package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.runner.config.InstrumentationParameters
import org.gradle.api.Project

/**
 * Instrumentation args consumed in `parseEnvironment()`
 *
 * TODO: Make stronger contract: MBS-7890
 */
internal class InstrumentationArgsResolver(
    private val analyticsResolver: AnalyticsResolver,
    private val buildEnvResolver: BuildEnvResolver,
    private val reportResolver: ReportResolver,
    private val planSlugResolver: PlanSlugResolver,
) {

    fun resolvePluginLevelParams(
        argsFromScript: Map<String, String>,
        project: Project,
        extension: InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration,
    ): Map<String, String> {
        val args = mutableMapOf<String, String>()
        args.resolveArg("planSlug", argsFromScript) {
            planSlugResolver.generateDefaultPlanSlug(project.path)
        }
        args.resolveArg("jobSlug", argsFromScript) {
            "LocalTests" // todo could be configuration name
        }
        args.resolveArg("runId", argsFromScript) {
            reportResolver.getRunId(extension)
        }
        args.resolveArg("teamcityBuildId", argsFromScript) {
            buildEnvResolver.getBuildId(project)
        }
        args.resolveArg("avito.report.enabled", argsFromScript) {
            project.getBooleanProperty("avito.report.enabled", default = false).toString()
        }
        args.resolveArg("fileStorageUrl", argsFromScript) {
            reportResolver.getFileStorageUrl(extension)
        }
        args.resolveArg("sentryDsn", argsFromScript) {
            analyticsResolver.getSentryDsn(extension)
        }
        args.resolveArg("deviceName", argsFromScript) {
            "local"
        }
        args.resolveArg("reportApiUrl", argsFromScript) {
            reportResolver.getReportApiUrl(extension)
        }
        args.resolveArg("reportViewerUrl", argsFromScript) {
            reportResolver.getReportViewerUrl(extension)
        }
        return args
    }

    fun getInstrumentationParams(
        extension: InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration,
        pluginLevelInstrumentationArgs: Map<String, String>,
    ): InstrumentationParameters {
        return InstrumentationParameters()
            .applyParameters(pluginLevelInstrumentationArgs)
            .applyParameters(extension.instrumentationParams)
    }

    private fun MutableMap<String, String>.resolveArg(
        key: String,
        argsFromScript: Map<String, String>,
        valueFromExtension: () -> String
    ) {
        val finalValue = if (argsFromScript.containsKey(key)) {
            val valueFromScript = argsFromScript[key]
            if (valueFromScript.isNullOrBlank()) {
                valueFromExtension.invoke()
            } else {
                valueFromScript
            }
        } else {
            valueFromExtension.invoke()
        }
        put(key, finalValue)
    }
}
